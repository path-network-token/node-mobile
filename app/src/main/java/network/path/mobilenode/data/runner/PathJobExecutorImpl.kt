package network.path.mobilenode.data.runner

import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.IO
import kotlinx.coroutines.experimental.async
import network.path.mobilenode.domain.PathJobExecutor
import network.path.mobilenode.domain.entity.JobRequest
import network.path.mobilenode.domain.entity.JobResult
import kotlin.coroutines.experimental.CoroutineContext

class PathJobExecutorImpl(private val runners: Runners) : PathJobExecutor, CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    override fun execute(request: JobRequest): Deferred<JobResult> {
        val runner = runners[request]
        return async { runner.runJob(request) }
    }
}
