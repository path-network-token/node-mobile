package network.path.mobilenode.runner

import network.path.mobilenode.message.JobRequest
import network.path.mobilenode.message.JobResult

interface Runner {
    suspend fun runJob(jobRequest: JobRequest): JobResult
}