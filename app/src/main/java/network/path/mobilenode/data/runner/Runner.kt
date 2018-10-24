package network.path.mobilenode.data.runner

import network.path.mobilenode.domain.entity.message.JobRequest
import network.path.mobilenode.domain.entity.message.JobResult

interface Runner {
    val checkType: CheckType
    suspend fun runJob(jobRequest: JobRequest): JobResult
}