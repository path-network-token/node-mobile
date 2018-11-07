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
import kotlin.math.max

class PathHttpEngine(
        private val job: Job,
        private val lastLocationProvider: LastLocationProvider,
        okHttpClient: OkHttpClient,
        gson: Gson,
        private val storage: PathStorage
) : PathEngine, CoroutineScope {
    companion object {
        private const val HEARTBEAT_INTERVAL_MS = 30_000L
        private const val POLL_INTERVAL_MS = 30_000L
        private const val MAX_JOBS = 10
    }

    private val currentExecutionUuids = mutableSetOf<String>()

    private val httpService = PathServiceImpl(okHttpClient, gson)
    private var timeoutJob = Job()
    private var pollJob = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override val status = ConflatedBroadcastChannel(ConnectionStatus.DISCONNECTED)
    override val requests = ConflatedBroadcastChannel<JobRequest>()
    override val nodeId = ConflatedBroadcastChannel(storage.nodeId)
    override val jobList = ConflatedBroadcastChannel<JobList>()

    override fun start() {
        checkIn()
        pollJobs()
    }

    override fun sendResult(result: JobResult) {
        if (result.executionUuid == "DUMMY_UUID") return

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
        pollJob.cancel()
        timeoutJob.cancel()
    }

    private fun checkIn() = launch {
        performCheckIn()
    }

    private suspend fun performCheckIn() {
        try {
            processJobs(httpService.checkIn(storage.nodeId ?: "", createCheckInMessage()).await())
        } catch (e: Exception) {
            Timber.w(e)
            status.send(ConnectionStatus.DISCONNECTED)
            scheduleCheckIn()
        }
    }

    private suspend fun processJobs(jobList: JobList) {
        Timber.d("HTTP: received job list [$jobList]")
        if (jobList.nodeId != null) {
            nodeId.send(jobList.nodeId)
        }
        this.jobList.send(jobList)

        status.send(ConnectionStatus.CONNECTED)

        if (jobList.jobs.isNotEmpty()) {
            val ids = jobList.jobs.map { it.executionUuid }
            currentExecutionUuids.addAll(ids)
        }
        scheduleCheckIn()

//        test()
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
        val jobsToRequest = max(MAX_JOBS - currentExecutionUuids.size, 0)
        return CheckIn(
                nodeId = storage.nodeId,
                wallet = storage.walletAddress,
                lat = location?.latitude?.toString(),
                lon = location?.longitude?.toString(),
                returnJobsMax = jobsToRequest
        )
    }

    private fun pollJobs() {
        Timber.d("HTTP: Start processing jobs...")
        pollJob.cancel()
        pollJob = launch {
            if (currentExecutionUuids.isNotEmpty()) {
                val ids = currentExecutionUuids.toList()
                Timber.d("HTTP: ${ids.size} jobs to be processed")
                ids.forEach { processJob(it) }
            }
            Timber.d("HTTP: Scheduling next jobs processing cycle...")
            delay(POLL_INTERVAL_MS)
            pollJobs()
        }
    }

    private fun scheduleCheckIn() {
        Timber.d("HTTP: Scheduling check in...")
        timeoutJob.cancel()
        timeoutJob = launch {
            delay(HEARTBEAT_INTERVAL_MS)
            Timber.d("HTTP: Checking in...")
            performCheckIn()
        }
    }

    private suspend fun test() {
        val dummyRequest = JobRequest(protocol = "tcp", payload = "HELO\n", endpointAddress = "smtp.gmail.com", endpointPort = 587, jobUuid = "DUMMY_UUID", executionUuid = "DUMMY_UUID")
//        val dummyRequest = JobRequest(protocol = "tcp", payload = "GET /\n\n", endpointAddress = "www.google.com", endpointPort = 80, jobUuid = "DUMMY_UUID", executionUuid = "DUMMY_UUID")
//        val dummyRequest = JobRequest(protocol = "", method="traceroute", payload = "HELLO", endpointAddress = "www.google.com", jobUuid = "DUMMY_UUID", executionUuid = "DUMMY_UUID")
        requests.send(dummyRequest)
    }
}
