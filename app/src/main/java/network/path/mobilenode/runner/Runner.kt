package network.path.mobilenode.runner

import network.path.mobilenode.message.JobRequest
import network.path.mobilenode.message.JobResult

interface Runner {
    val checkType: CheckType
    suspend fun runJob(jobRequest: JobRequest): JobResult
}