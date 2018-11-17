package network.path.mobilenode.domain

import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.IO
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import network.path.mobilenode.domain.entity.JobRequest
import timber.log.Timber
import kotlin.coroutines.experimental.CoroutineContext

class PathSystem(
        private val job: Job,
        private val engine: PathEngine,
        private val storage: PathStorage,
        private val externalServices: PathExternalServices,
        private val jobExecutor: PathJobExecutor
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    val ip get() = externalServices.ip
    val details get() = externalServices.details
    val status get() = engine.status
    val nodeId get() = engine.nodeId
    val jobList get() = engine.jobList
    val isRunning get() = engine.isRunning

    init {
        registerJobRequestHandler()
        registerNodeIdHandler()
    }

    fun start() {
        engine.start()
        externalServices.start()
    }

    fun toggle() {
        engine.toggle()
    }

    fun stop() {
        engine.stop()
        externalServices.stop()
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
        val result = jobExecutor.execute(request).await()
        storage.recordStatistics(result.checkType, result.responseTime)
        engine.processResult(result)
        Timber.d("SYSTEM: request result [$result]")
    }
}
