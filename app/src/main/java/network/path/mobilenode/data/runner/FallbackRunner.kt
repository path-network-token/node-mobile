package network.path.mobilenode.data.runner

import network.path.mobilenode.domain.entity.CheckType
import network.path.mobilenode.domain.entity.JobRequest
import network.path.mobilenode.domain.entity.JobResult
import network.path.mobilenode.domain.entity.Status

object FallbackRunner : Runner {
    override val checkType = CheckType.UNKNOWN

    override suspend fun runJob(jobRequest: JobRequest) = JobResult(
            checkType = checkType,
            responseBody = "No runner found for $jobRequest",
            responseTime = 0L,
            status = Status.UNKNOWN,
            executionUuid = jobRequest.executionUuid
    )
}