package network.path.mobilenode.data.runner

import network.path.mobilenode.domain.entity.CheckType
import network.path.mobilenode.domain.entity.JobRequest
import network.path.mobilenode.domain.entity.JobResult

interface Runner {
    val checkType: CheckType
    suspend fun runJob(jobRequest: JobRequest): JobResult
}