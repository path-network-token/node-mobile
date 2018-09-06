package pl.droidsonroids.minertest.runner

import android.os.SystemClock
import pl.droidsonroids.minertest.message.JobRequest
import pl.droidsonroids.minertest.message.JobResult
import pl.droidsonroids.minertest.message.Status
import java.io.IOException

private const val DEGRADED_TIMEOUT_MILLIS = 1000L
private const val CRITICAL_TIMEOUT_MILLIS = 1000L

fun getRunner(jobRequest: JobRequest) = when {
    jobRequest.protocol.startsWith(prefix = "http", ignoreCase = true) -> HttpRunner()
    jobRequest.protocol.startsWith(prefix = "tcp", ignoreCase = true) -> TcpRunner()
    else -> null
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
        requestDurationMillis = 0L
        responseBody = e.message ?: ""
        status = Status.unknown
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
    val degradedAfterMillis = jobRequest.degradedAfter ?: DEGRADED_TIMEOUT_MILLIS
    val criticalAfterMillis = jobRequest.criticalAfter ?: CRITICAL_TIMEOUT_MILLIS

    return when {
        requestDurationMillis > degradedAfterMillis -> Status.degraded
        requestDurationMillis > criticalAfterMillis -> Status.critical
        else -> Status.ok
    }
}