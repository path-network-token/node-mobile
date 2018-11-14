package network.path.mobilenode.data.http

import com.google.gson.Gson
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Deferred
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
import retrofit2.HttpException
import timber.log.Timber
import java.net.InetSocketAddress
import java.net.Proxy
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.math.max

class PathHttpEngine(
        private val job: Job,
        private val lastLocationProvider: LastLocationProvider,
        private val okHttpClient: OkHttpClient,
        private val gson: Gson,
        private val storage: PathStorage
) : PathEngine, CoroutineScope {
    companion object {
        private const val HEARTBEAT_INTERVAL_MS = 30_000L
        private const val POLL_INTERVAL_MS = 9_000L
        private const val MAX_JOBS = 10
    }

    private val currentExecutionUuids = java.util.concurrent.ConcurrentHashMap<String, Boolean>()

    private var httpService = getHttpService(false)
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

    override fun processResult(result: JobResult) {
        if (result.executionUuid == "DUMMY_UUID") return

        val nodeId = storage.nodeId ?: return
        launch {
            executeServiceCall {
                httpService.postResult(nodeId, result.executionUuid, result)
            }
        }

        currentExecutionUuids.remove(result.executionUuid)

        val inactiveUuids = currentExecutionUuids.filterValues { !it }
        Timber.d("HTTP: ${currentExecutionUuids.size} jobs in the pool, ${inactiveUuids.size} jobs not yet active...")
//        if (inactiveUuids.isEmpty()) {
//            checkIn()
//        }
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
        val result = executeServiceCall {
            httpService.checkIn(storage.nodeId ?: "", createCheckInMessage())
        }
        if (result != null) {
            processJobs(result)
        } else {
            status.send(ConnectionStatus.DISCONNECTED)
            scheduleCheckIn()
        }
    }

    private suspend fun processJobs(list: JobList) {
        Timber.d("HTTP: received job list [$list]")
        if (list.nodeId != null) {
            nodeId.send(list.nodeId)
        }
        jobList.send(list)

        status.send(ConnectionStatus.CONNECTED)

        if (list.jobs.isNotEmpty()) {
            val ids = list.jobs.map { it.executionUuid to false }
            // Add new jobs to the pool
            currentExecutionUuids.putAll(ids)
        }
        scheduleCheckIn()

//        test()
    }

    private suspend fun processJob(executionUuid: String) {
        val details = executeServiceCall {
            httpService.requestDetails(executionUuid)
        }
        if (details != null) {
            details.executionUuid = executionUuid
            // Mark job as active
            currentExecutionUuids[executionUuid] = true
            requests.send(details)
        } else {
            currentExecutionUuids.remove(executionUuid)
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
                // Process only jobs which were not marked as active.
                val ids = currentExecutionUuids.filterValues { !it }.keys.toList()
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

    private suspend fun <T> executeServiceCall(call: suspend () -> Deferred<T>): T? {
        return try {
            call().await()
        } catch (e: Exception) {
            if (e is HttpException) {
                if (e.code() == 422) {
                    val body = e.response()?.body()
                    Timber.w("HTTP exception: $body")
                    // TODO: Parse
                } else {
                    httpService = getHttpService(true)
                }
            }
            Timber.w("Service call exception: $e", e)
            null
        }
    }

    private fun getHttpService(useProxy: Boolean): PathService {
        val client = if (!useProxy) okHttpClient else okHttpClient
                .newBuilder()
                .proxy(Proxy(Proxy.Type.SOCKS, InetSocketAddress.createUnresolved("127.0.0.1", 1081)))
                .build()
        return PathServiceImpl(client, gson)
    }
}
