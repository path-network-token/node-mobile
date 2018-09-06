package pl.droidsonroids.minertest.runner

import pl.droidsonroids.minertest.message.JobRequest
import pl.droidsonroids.minertest.message.JobResult

interface Runner {
    fun runJob(jobRequest: JobRequest): JobResult
}