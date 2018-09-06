package pl.droidsonroids.minertest.runner

import android.os.SystemClock
import pl.droidsonroids.minertest.message.JobRequest
import pl.droidsonroids.minertest.message.JobResult
import pl.droidsonroids.minertest.message.Status
import java.io.IOException

fun getRunner(jobRequest: JobRequest) = when {
    jobRequest.protocol.startsWith(prefix = "http", ignoreCase = true) -> HttpRunner()
    jobRequest.protocol.startsWith(prefix = "tcp", ignoreCase = true) -> TcpRunner()
    else -> throw IOException("No runner found for: $jobRequest")
}

fun computeJobResult(jobRequest: JobRequest, block: (JobRequest) -> String): JobResult {
    var responseBody = ""
    var status: Status
    var requestDurationMillis: Long

    try {
        requestDurationMillis = measureRealtimeMillis {
            responseBody = block(jobRequest)
        }

        status = calculateJobStatus(requestDurationMillis, jobRequest)
    } catch (e: IOException) {
        status = Status.unknown
        requestDurationMillis = 0L
    }

    return JobResult(
        jobUuid = jobRequest.jobUuid,
        responseTime = requestDurationMillis,
        responseBody = responseBody,
        status = status
    )
}

inline fun measureRealtimeMillis(block: () -> Unit): Long {
    val start = SystemClock.elapsedRealtime()
    block()
    return SystemClock.elapsedRealtime() - start
}

fun calculateJobStatus(requestDurationMillis: Long, jobRequest: JobRequest): Status {
    val degradedAfterMillis = jobRequest.degradedAfter ?: 1000L
    val criticalAfterMillis = jobRequest.criticalAfter ?: 2000L

    return when {
        requestDurationMillis > degradedAfterMillis -> Status.degraded
        requestDurationMillis > criticalAfterMillis -> Status.critical
        else -> Status.ok
    }
}