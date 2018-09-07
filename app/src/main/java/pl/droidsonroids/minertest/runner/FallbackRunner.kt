package pl.droidsonroids.minertest.runner

import pl.droidsonroids.minertest.message.JobRequest
import pl.droidsonroids.minertest.message.JobResult
import pl.droidsonroids.minertest.message.Status

object FallbackRunner : Runner {
    override suspend fun runJob(jobRequest: JobRequest) = JobResult(
        responseBody = "No runner found for $jobRequest",
        responseTime = 0L,
        status = Status.unknown,
        jobUuid = jobRequest.jobUuid
    )
}