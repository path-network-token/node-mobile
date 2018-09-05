package pl.droidsonroids.minertest.runner

import pl.droidsonroids.minertest.message.JobRequest
import java.io.IOException

fun getRunner(jobRequest: JobRequest) = when {
    jobRequest.protocol.startsWith(prefix = "http", ignoreCase = true) -> HttpRunner()
    jobRequest.protocol.startsWith(prefix = "tcp", ignoreCase = true) -> TcpRunner()
    else -> throw IOException("No runner found for: $jobRequest")
}