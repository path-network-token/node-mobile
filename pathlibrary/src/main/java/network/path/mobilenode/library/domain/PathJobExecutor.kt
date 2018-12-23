package network.path.mobilenode.library.domain

import kotlinx.coroutines.Deferred
import network.path.mobilenode.library.domain.entity.JobRequest
import network.path.mobilenode.library.domain.entity.JobResult

interface PathJobExecutor {
    fun execute(request: JobRequest): Deferred<JobResult>
    fun start()
    fun stop()
}
