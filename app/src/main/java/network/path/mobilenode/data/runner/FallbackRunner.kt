package network.path.mobilenode.data.runner

import network.path.mobilenode.data.json.Status
import network.path.mobilenode.domain.entity.message.JobRequest
import network.path.mobilenode.domain.entity.message.JobResult

object FallbackRunner : Runner {

    override val checkType = CheckType.UNKNOWN

    override suspend fun runJob(jobRequest: JobRequest) = JobResult(
        responseBody = "No runner found for $jobRequest",
        responseTime = 0L,
        status = Status.UNKNOWN,
        jobUuid = jobRequest.jobUuid
    )
}