package pl.droidsonroids.minertest.runner

import pl.droidsonroids.minertest.json.Status
import pl.droidsonroids.minertest.message.JobRequest
import pl.droidsonroids.minertest.message.JobResult

object FallbackRunner : Runner {
    override suspend fun runJob(jobRequest: JobRequest) = JobResult(
        responseBody = "No runner found for $jobRequest",
        responseTime = 0L,
        status = Status.UNKNOWN,
        jobUuid = jobRequest.jobUuid
    )
}