package network.path.mobilenode.library.domain

import android.annotation.SuppressLint
import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.instacart.library.truetime.TrueTimeRx
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import network.path.mobilenode.library.BuildConfig
import network.path.mobilenode.library.Constants
import network.path.mobilenode.library.data.android.LastLocationProvider
import network.path.mobilenode.library.data.android.NetworkMonitor
import network.path.mobilenode.library.data.http.CustomDns
import network.path.mobilenode.library.data.http.PathHttpEngine
import network.path.mobilenode.library.data.runner.PathJobExecutorImpl
import network.path.mobilenode.library.data.storage.PathStorageImpl
import network.path.mobilenode.library.domain.entity.CheckType
import network.path.mobilenode.library.domain.entity.CheckTypeStatistics
import network.path.mobilenode.library.domain.entity.JobRequest
import network.path.mobilenode.library.utils.Executable
import network.path.mobilenode.library.utils.GuardedProcessPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

@InternalCoroutinesApi
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class PathSystem(
        private val context: Context,
        private val engine: PathEngine,
        val storage: PathStorage,
        private val jobExecutor: PathJobExecutor,
        private val networkMonitor: NetworkMonitor
) : CoroutineScope {
    companion object {
        private const val TIMEOUT = 600
        private const val PROXY_RESTART_TIMEOUT = 3_600_000L // 1 hour

        private const val PROXY_HOST = "afiasvoiuasd.net"
        private const val PROXY_PORT = 443
        private const val PROXY_PASSWORD = "PathNetwork"
        private const val PROXY_ENCRYPTION_METHOD = "aes-256-cfb"

        fun create(context: Context): PathSystem {
            val gson = createLenientGson()
            val okHttpClient = createOkHttpClient()
            val networkMonitor = NetworkMonitor(context)
            val locationProvider = LastLocationProvider(context)
            val storage = PathStorageImpl(context)
            val engine = PathHttpEngine(locationProvider, networkMonitor, okHttpClient, gson, storage)
            val jobExecutor = PathJobExecutorImpl(okHttpClient, storage, gson)
            return PathSystem(context, engine, storage, jobExecutor, networkMonitor)
        }

        private fun createLenientGson(): Gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()

        private fun createOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
                .readTimeout(Constants.JOB_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                .writeTimeout(Constants.JOB_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                .connectTimeout(Constants.JOB_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                .addInterceptor { chain ->
                    try {
                        chain.proceed(chain.request())
                    } catch (e: Throwable) {
                        throw if (e is IOException) e else IOException(e)
                    }
                }
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
//            level = HttpLoggingInterceptor.Level.BODY
                })
                .dns(CustomDns())
                .build()

    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    val status get() = engine.status
    val nodeId get() = engine.nodeId
    val jobList get() = engine.jobList
    val isRunning get() = engine.isRunning
    val statistics = ConflatedBroadcastChannel<List<CheckTypeStatistics>>()

    private var timerJob: Job? = null
    private val ssLocal = GuardedProcessPool()
    private val simpleObfs = GuardedProcessPool()

    init {
        initTrueTime()
        registerJobRequestHandler()
        registerNodeIdHandler()
    }

    fun start() {
        jobExecutor.start()
        networkMonitor.start()
        engine.start()

        // Initial statistics value
        launch {
            sendStatistics()
        }

        // Native processes
        launch {
            startNativeProcesses()
        }
        scheduleNativeRestart()
    }

    fun toggle() {
        engine.toggle()
    }

    fun stop() {
        engine.stop()
        networkMonitor.stop()
        jobExecutor.stop()
        // Kill them all
        Executable.killAll()
    }

    private fun registerJobRequestHandler() = launch {
        engine.requests.consumeEach {
            process(it)
        }
    }

    private fun registerNodeIdHandler() = launch {
        engine.nodeId.consumeEach {
            if (it != null) {
                // Update nodeId in storage if it is not null
                storage.nodeId = it
            }
        }
    }

    private suspend fun process(request: JobRequest) {
        Timber.d("SYSTEM: received [$request]")
        val result = jobExecutor.execute(request).get()
        storage.recordStatistics(result.checkType, result.responseTime)
        engine.processResult(result)
        sendStatistics()
        Timber.d("SYSTEM: request result [$result]")
    }

    private suspend fun sendStatistics() {
        val allStats = CheckType.values()
                .map { storage.statisticsForType(it) }
                .sortedWith(compareByDescending(CheckTypeStatistics::count)
                        .then(compareByDescending(CheckTypeStatistics::averageLatency)))

        val otherStats = allStats.subList(2, allStats.size - 1)
                .fold(CheckTypeStatistics(null, 0, 0)) { total, stats ->
                    total.addOther(stats)
                }

        statistics.send(listOf(allStats[0], allStats[1], otherStats))
    }

    private fun startNativeProcesses() {
        val host = DomainGenerator.findDomain(storage) ?: PROXY_HOST
        if (host != null) {
            Timber.d("PATH SERVICE: found proxy domain [$host]")
            Executable.killAll()

            val libs = context.applicationInfo.nativeLibraryDir
            val obfsCmd = mutableListOf(
                    File(libs, Executable.SIMPLE_OBFS).absolutePath,
                    "-s", host,
                    "-p", PROXY_PORT.toString(),
                    "-l", Constants.SIMPLE_OBFS_PORT.toString(),
                    "-t", TIMEOUT.toString(),
                    "--obfs", "http"
            )
            if (BuildConfig.DEBUG) {
                obfsCmd.add("-v")
            }
            simpleObfs.start(obfsCmd)

            val cmd = mutableListOf(
                    File(libs, Executable.SS_LOCAL).absolutePath,
                    "-u",
                    "-s", Constants.LOCALHOST,
                    "-p", Constants.SIMPLE_OBFS_PORT.toString(),
                    "-k", PROXY_PASSWORD,
                    "-m", PROXY_ENCRYPTION_METHOD,
                    "-b", Constants.LOCALHOST,
                    "-l", Constants.SS_LOCAL_PORT.toString(),
                    "-t", TIMEOUT.toString()
            )
            if (BuildConfig.DEBUG) {
                cmd.add("-v")
            }

            ssLocal.start(cmd)
        } else {
            Timber.w("PATH SERVICE: proxy domain not found")
        }
    }

    private fun scheduleNativeRestart() {
        timerJob?.cancel()
        timerJob = GlobalScope.launch(Dispatchers.IO) {
            delay(PROXY_RESTART_TIMEOUT)
            startNativeProcesses()
            scheduleNativeRestart()
        }
    }

    @SuppressLint("CheckResult")
    private fun initTrueTime() {
        TrueTimeRx.build()
                .initializeRx("time.google.com")
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { date -> Timber.d("TRUE TIME: initialised [$date]") },
                        { throwable -> Timber.w("TRUE TIME: initialisation failed: $throwable") }
                )
    }
}
