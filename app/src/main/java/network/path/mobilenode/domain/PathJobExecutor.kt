package network.path.mobilenode.domain

import kotlinx.coroutines.Deferred
import network.path.mobilenode.domain.entity.JobRequest
import network.path.mobilenode.domain.entity.JobResult

interface PathJobExecutor {
    fun execute(request: JobRequest): Deferred<JobResult>
    fun start()
    fun stop()
}
