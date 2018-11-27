package network.path.mobilenode.data.http

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import network.path.mobilenode.domain.PathEngine
import network.path.mobilenode.domain.PathStorage
import network.path.mobilenode.domain.entity.CheckIn
import network.path.mobilenode.domain.entity.ConnectionStatus
import network.path.mobilenode.domain.entity.JobList
import network.path.mobilenode.domain.entity.JobRequest
import network.path.mobilenode.domain.entity.JobResult
import network.path.mobilenode.service.ForegroundService
import network.path.mobilenode.service.LastLocationProvider
import network.path.mobilenode.service.NetworkMonitor
import okhttp3.OkHttpClient
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ServerSocket
import kotlin.coroutines.CoroutineContext
import kotlin.math.max

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class PathHttpEngine(
        private val job: Job,
        private val lastLocationProvider: LastLocationProvider,
        private val networkMonitor: NetworkMonitor,
        private val okHttpClient: OkHttpClient,
        private val gson: Gson,
        private val storage: PathStorage
) : PathEngine, CoroutineScope {
    companion object {
        private const val HEARTBEAT_INTERVAL_MS = 30_000L
        private const val POLL_INTERVAL_MS = 9_000L
        private const val MAX_JOBS = 10
        private const val MAX_RETRIES = 5
    }

    private val currentExecutionUuids = java.util.concurrent.ConcurrentHashMap<String, Boolean>()

    private var retryCounter = 0
    private var useProxy = false
    private var httpService: PathService? = null
    private var timeoutJob = Job()
    private var pollJob = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override val status = ConflatedBroadcastChannel(ConnectionStatus.DISCONNECTED)
    override val requests = ConflatedBroadcastChannel<JobRequest>()
    override val nodeId = ConflatedBroadcastChannel(storage.nodeId)
    override val jobList = ConflatedBroadcastChannel<JobList>()
    override var isRunning = ConflatedBroadcastChannel(true)

    private var _isRunning = true
        set(value) {
            field = value
            launch {
                isRunning.send(value)
            }
        }

    init {
        registerNetworkHandler()
    }

    override fun start() {
        launch {
            delay(1000)
            httpService = getHttpService(false)
            checkIn()
            pollJobs()
        }

//        kotlin.concurrent.fixedRateTimer("TEST", false, java.util.Date(), 5_000) {
//            launch {
//                status.send(if (status.valueOrNull == ConnectionStatus.CONNECTED) ConnectionStatus.DISCONNECTED else ConnectionStatus.CONNECTED)
//            }
//        }
    }

    override fun processResult(result: JobResult) {
        if (result.executionUuid == "DUMMY_UUID") return

        val nodeId = storage.nodeId ?: return
        launch {
            executeServiceCall {
                httpService?.postResult(nodeId, result.executionUuid, result)
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

    override fun toggle() {
        _isRunning = !_isRunning
        Timber.d("HTTP: changed status to [$_isRunning]")
    }

    private fun checkIn() = launch {
        performCheckIn()
    }

    private fun registerNetworkHandler() = launch {
        networkMonitor.connected.consumeEach {
            timeoutJob.cancel()
            delay(500L)
            performCheckIn()
        }
    }

    private suspend fun performCheckIn() {
        val result = executeServiceCall {
            httpService?.checkIn(storage.nodeId ?: "", createCheckInMessage())
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

        status.send(if (useProxy) ConnectionStatus.PROXY else ConnectionStatus.CONNECTED)

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
            httpService?.requestDetails(executionUuid)
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
        val location = try {
            lastLocationProvider.getLastRealLocationOrNull()
        } catch (e: Exception) {
            null
        }
        val jobsToRequest = if (_isRunning) max(MAX_JOBS - currentExecutionUuids.size, 0) else 0
        return CheckIn(
                nodeId = storage.nodeId,
                wallet = storage.walletAddress,
                lat = location?.latitude?.toString() ?: "0.0",
                lon = location?.longitude?.toString() ?: "0.0",
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
        if (!timeoutJob.isCancelled) {
            timeoutJob.cancel()
        }
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

    private suspend fun <T> executeServiceCall(call: suspend () -> Deferred<T>?): T? {
        return try {
            val result = call()?.await()
            if (result != null) {
                retryCounter = 0
            }
            result
        } catch (e: Exception) {
            var fallback = true
            if (e is HttpException) {
                if (e.code() == 422) {
                    val body = e.response()?.body()
                    Timber.w("HTTP exception: $body")
                    // TODO: Parse
                    fallback = false
                }
            }
            if (fallback) {
                if (++retryCounter >= MAX_RETRIES) {
                    Timber.w("HTTP: switching proxy mode to [${!useProxy}]")
                    retryCounter = 0
                    httpService = getHttpService(!useProxy)
                }
            }
            Timber.w("HTTP: Service call exception: $e")
            null
        }
    }

    private fun getHttpService(useProxy: Boolean): PathService {
        val host = ForegroundService.LOCALHOST
        val port = ForegroundService.SS_LOCAL_PORT

        Timber.d("HTTP: creating new service [$useProxy]...")

        // Verify that ss-local is actually running before using it as a proxy
        val client = if (useProxy && isPortInUse(port)) {
            Timber.d("HTTP: proxy port [$port] is in use, connecting")
            this.useProxy = true
            okHttpClient.newBuilder()
                    .proxy(Proxy(Proxy.Type.SOCKS, InetSocketAddress.createUnresolved(host, port)))
                    .build()
        } else {
            if (useProxy) {
                Timber.d("HTTP: proxy port [$port] is not in use, proxy is not running")
            } else {
                Timber.d("HTTP: proxy is not required")
            }
            this.useProxy = false
            okHttpClient
        }
        return PathServiceImpl(client, gson)
    }

    private fun isPortInUse(port: Int) = try {
        ServerSocket(port).close()
        false
    } catch (e: IOException) {
        true
    }
}
