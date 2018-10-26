package network.path.mobilenode.data.http

import com.google.gson.Gson
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.IO
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import network.path.mobilenode.domain.PathEngine
import network.path.mobilenode.domain.PathStorage
import network.path.mobilenode.domain.entity.CheckIn
import network.path.mobilenode.domain.entity.ConnectionStatus
import network.path.mobilenode.domain.entity.JobList
import network.path.mobilenode.domain.entity.JobRequest
import network.path.mobilenode.domain.entity.JobResult
import network.path.mobilenode.service.LastLocationProvider
import okhttp3.OkHttpClient
import timber.log.Timber
import kotlin.coroutines.experimental.CoroutineContext

class PathHttpEngine(
        private val job: Job,
        private val lastLocationProvider: LastLocationProvider,
        okHttpClient: OkHttpClient,
        gson: Gson,
        private val storage: PathStorage
) : PathEngine, CoroutineScope {
    companion object {
        private const val HEARTBEAT_INTERVAL_MILLIS = 30_000L
    }

    private val currentExecutionUuids = mutableSetOf<String>()

    private val httpService = PathServiceImpl(okHttpClient, gson)
    private var timeoutJob = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override val status = ConflatedBroadcastChannel(ConnectionStatus.DISCONNECTED)
    override val requests = ConflatedBroadcastChannel<JobRequest>()
    override val nodeId = ConflatedBroadcastChannel(storage.nodeId)

    override fun start() {
        checkIn()
    }

    override fun sendResult(result: JobResult) {
        val nodeId = storage.nodeId ?: return
        try {
            httpService.postResult(nodeId, result.executionUuid, result)
        } catch (e: Exception) {
            Timber.w(e)
        }

        currentExecutionUuids.remove(result.executionUuid)
        Timber.d("HTTP: ${currentExecutionUuids.size} jobs left to process...")
        if (currentExecutionUuids.isEmpty()) {
            checkIn()
        }
    }

    override fun stop() {
        job.cancel()
    }

    private fun checkIn() = launch {
        performCheckIn()
    }

    private suspend fun performCheckIn() {
        try {
            processJobs(httpService.checkIn(storage.nodeId, createCheckInMessage()).await())
        } catch (e: Exception) {
            Timber.w(e)
            status.send(ConnectionStatus.DISCONNECTED)
            scheduleCheckIn()
        }
    }

    private suspend fun processJobs(jobList: JobList) {
        if (jobList.nodeId != null) {
            nodeId.send(jobList.nodeId)
        }

        status.send(ConnectionStatus.CONNECTED)
        if (jobList.jobs.isEmpty()) {
            scheduleCheckIn()
        } else {
            val uuids = jobList.jobs.map { it.executionUuid }
            currentExecutionUuids.addAll(uuids)
            uuids.forEach { processJob(it) }
        }
    }

    private suspend fun processJob(executionUuid: String) {
        try {
            val details = httpService.requestDetails(executionUuid).await()
            details.executionUuid = executionUuid
            requests.send(details)
        } catch (e: Exception) {
            Timber.w(e)
            // TODO: Error handling?
        }
    }

    private suspend fun createCheckInMessage(): CheckIn {
        val location = lastLocationProvider.getLastRealLocationOrNull()
        return CheckIn(
                nodeId = storage.nodeId,
                wallet = storage.walletAddress,
                lat = location?.latitude?.toString(),
                lon = location?.longitude?.toString()
        )
    }

    private fun scheduleCheckIn() {
        Timber.d("HTTP: Scheduling check in...")
        timeoutJob.cancel()
        timeoutJob = launch {
            delay(HEARTBEAT_INTERVAL_MILLIS)
            Timber.d("HTTP: Checking in...")
            performCheckIn()
        }
    }
}
