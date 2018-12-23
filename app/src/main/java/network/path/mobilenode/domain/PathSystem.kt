package network.path.mobilenode.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import network.path.mobilenode.domain.entity.CheckType
import network.path.mobilenode.domain.entity.CheckTypeStatistics
import network.path.mobilenode.domain.entity.JobRequest
import network.path.mobilenode.service.NetworkMonitor
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class PathSystem(
        private val job: Job,
        private val engine: PathEngine,
        private val storage: PathStorage,
        private val jobExecutor: PathJobExecutor,
        private val networkMonitor: NetworkMonitor
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    val status get() = engine.status
    val nodeId get() = engine.nodeId
    val jobList get() = engine.jobList
    val isRunning get() = engine.isRunning
    val statistics = ConflatedBroadcastChannel<List<CheckTypeStatistics>>()

    init {
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
    }

    fun toggle() {
        engine.toggle()
    }

    fun stop() {
        engine.stop()
        networkMonitor.stop()
        jobExecutor.stop()
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
}
