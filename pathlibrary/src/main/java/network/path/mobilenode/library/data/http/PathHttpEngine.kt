package network.path.mobilenode.library.data.http

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import network.path.mobilenode.library.Constants
import network.path.mobilenode.library.data.android.LastLocationProvider
import network.path.mobilenode.library.data.android.NetworkMonitor
import network.path.mobilenode.library.domain.PathEngine
import network.path.mobilenode.library.domain.PathStorage
import network.path.mobilenode.library.domain.WifiSetting
import network.path.mobilenode.library.domain.entity.CheckIn
import network.path.mobilenode.library.domain.entity.ConnectionStatus
import network.path.mobilenode.library.domain.entity.JobList
import network.path.mobilenode.library.domain.entity.JobRequest
import network.path.mobilenode.library.domain.entity.JobResult
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ServerSocket
import kotlin.coroutines.CoroutineContext
import kotlin.math.max

@InternalCoroutinesApi
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class PathHttpEngine(
        private val lastLocationProvider: LastLocationProvider,
        private val networkMonitor: NetworkMonitor,
        private val okHttpClient: OkHttpClient,
        private val gson: Gson,
        private val storage: PathStorage
) : PathEngine, NetworkMonitor.Listener, CoroutineScope {
    companion object {
        private const val HEARTBEAT_INTERVAL_MS = 30_000L
        private const val HEARTBEAT_INTERVAL_ERROR_MS = 5_000L
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
        get() = Dispatchers.IO

    override val status = ConflatedBroadcastChannel(ConnectionStatus.LOOKING)
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

    override fun start() {
        networkMonitor.addListener(this)

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
        pollJob.cancel()
        timeoutJob.cancel()

        networkMonitor.removeListener(this)
    }

    override fun toggle() {
        _isRunning = !_isRunning
        Timber.d("HTTP: changed status to [$_isRunning]")
    }

    private fun checkIn() = launch {
        performCheckIn()
    }

    override fun onStatusChanged(connected: Boolean) {
        launch {
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
            if (status.value != ConnectionStatus.LOOKING) {
                status.send(ConnectionStatus.DISCONNECTED)
            }
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

    private fun createCheckInMessage(): CheckIn {
        val location = try {
            lastLocationProvider.location()
        } catch (e: Exception) {
            null
        }
        var requestJobs = true
        if (storage.wifiSetting == WifiSetting.WIFI_ONLY) {
            // Make sure we are on WiFi if wifiOnly setting is used
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val network = networkMonitor.connectivityManager.activeNetwork
                val capabilities = networkMonitor.connectivityManager.getNetworkCapabilities(network)
                requestJobs = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                Timber.d("HTTP: network [$network], setting [${storage.wifiSetting}, requestJobs = $requestJobs")
            } else {
                @Suppress("DEPRECATION")
                val networkInfo = networkMonitor.connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                requestJobs = networkInfo?.isConnected == true
                Timber.d("HTTP: network info [$networkInfo], setting [${storage.wifiSetting}], requestJobs = $requestJobs")
            }
        }
        val jobsToRequest = if (_isRunning && requestJobs) max(MAX_JOBS - currentExecutionUuids.size, 0) else 0
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
            delay(if (retryCounter > 0) HEARTBEAT_INTERVAL_ERROR_MS else HEARTBEAT_INTERVAL_MS)
            Timber.d("HTTP: Checking in...")
            performCheckIn()
        }
    }

    private suspend fun test() {
//        val dummyRequest = JobRequest(protocol = "tcp", payload = "HELO\n", endpointAddress = "smtp.gmail.com", endpointPort = 587, jobUuid = "DUMMY_UUID", executionUuid = "DUMMY_UUID")
//        val dummyRequest = JobRequest(protocol = "tcp", payload = "GET /\n\n", endpointAddress = "www.google.com", endpointPort = 80, jobUuid = "DUMMY_UUID", executionUuid = "DUMMY_UUID")
        val dummyRequest = JobRequest(protocol = "", method = "traceroute", payload = "HELLO", endpointAddress = "www.google.com", jobUuid = "DUMMY_UUID", executionUuid = "DUMMY_UUID")
        requests.send(dummyRequest)
    }

    private fun <T> executeServiceCall(call: () -> Call<T>?): T? {
        return try {
            val result = call()?.execute()?.body()
            if (result != null) {
                retryCounter = 0
            }
            result
        } catch (e: Exception) {
            var fallback = true
            if (e is HttpException) {
                if (e.code() == 422) {
                    val body = e.response().body()
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
        val host = Constants.LOCALHOST
        val port = Constants.SS_LOCAL_PORT

        Timber.d("HTTP: creating new service [$useProxy]...")

        // Verify that ss-local is actually running before using it as a proxy
        val client = if (useProxy && isPortInUse(port)) {
            Timber.d("HTTP: proxy port [$port] is in use, connecting")
            this.useProxy = true
            okHttpClient.newBuilder().addProxy(host, port).build()
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

    private fun OkHttpClient.Builder.addProxy(host: String, port: Int): OkHttpClient.Builder =
            proxy(Proxy(Proxy.Type.SOCKS, InetSocketAddress.createUnresolved(host, port)))
                    .addInterceptor { chain ->
                        val request = chain.request()
                        val url = request.url().newBuilder().scheme("http").build()
                        chain.proceed(request.newBuilder().url(url).build())
                    }
}
