package network.path.mobilenode.runner

import network.path.mobilenode.json.Status
import network.path.mobilenode.message.JobRequest
import network.path.mobilenode.message.JobResult

object FallbackRunner : Runner {

    override val checkType = CheckType.UNKNOWN

    override suspend fun runJob(jobRequest: JobRequest) = JobResult(
        responseBody = "No runner found for $jobRequest",
        responseTime = 0L,
        status = Status.UNKNOWN,
        jobUuid = jobRequest.jobUuid
    )
}