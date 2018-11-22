package network.path.mobilenode.data.runner

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import network.path.mobilenode.domain.PathJobExecutor
import network.path.mobilenode.domain.entity.JobRequest
import network.path.mobilenode.domain.entity.JobResult
import kotlin.coroutines.CoroutineContext

class PathJobExecutorImpl(private val runners: Runners) : PathJobExecutor, CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    override fun execute(request: JobRequest): Deferred<JobResult> {
        val runner = runners[request]
        return async { runner.runJob(request) }
    }
}
