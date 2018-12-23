package network.path.mobilenode.library.data.runner

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.async
import network.path.mobilenode.library.data.http.OkHttpWorkerPool
import network.path.mobilenode.library.domain.PathJobExecutor
import network.path.mobilenode.library.domain.PathStorage
import network.path.mobilenode.library.domain.entity.JobRequest
import network.path.mobilenode.library.domain.entity.JobResult
import okhttp3.OkHttpClient
import kotlin.coroutines.CoroutineContext

@InternalCoroutinesApi
class PathJobExecutorImpl(okHttpClient: OkHttpClient, private val storage: PathStorage, private val gson: Gson) : PathJobExecutor, CoroutineScope {
    private val workerPool = OkHttpWorkerPool(okHttpClient, 10)

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    override fun execute(request: JobRequest): Deferred<JobResult> {
        val runner = with(request) {
            when {
                protocol == null -> FallbackRunner
                protocol.startsWith(prefix = "http", ignoreCase = true) -> HttpRunner(workerPool, storage)
                protocol.startsWith(prefix = "tcp", ignoreCase = true) -> TcpRunner()
                protocol.startsWith(prefix = "udp", ignoreCase = true) -> UdpRunner()
                method.orEmpty().startsWith(prefix = "traceroute", ignoreCase = true) -> TracepathRunner(gson)
                else -> FallbackRunner
            }
        }
        return async { runner.runJob(request) }
    }

    override fun start() {
        workerPool.startWorkers()
    }

    override fun stop() {
        workerPool.close()
    }
}
