package pl.droidsonroids.minertest.runner

import android.os.SystemClock
import pl.droidsonroids.minertest.Constants.TCP_UDP_PORT_RANGE
import pl.droidsonroids.minertest.json.Status
import pl.droidsonroids.minertest.message.JobRequest
import pl.droidsonroids.minertest.message.JobResult
import java.io.IOException

private const val DEGRADED_TIMEOUT_MILLIS = 1000L
private const val CRITICAL_TIMEOUT_MILLIS = 2000L
private const val BODY_LENGTH_BYTES_MAX = 1 shl 15

fun JobRequest.getRunner() = when {
    protocol == null -> FallbackRunner
    protocol.startsWith(prefix = "http", ignoreCase = true) -> HttpRunner()
    protocol.startsWith(prefix = "tcp", ignoreCase = true) -> TcpRunner()
    protocol.startsWith(prefix = "udp", ignoreCase = true) -> UdpRunner()
    else -> FallbackRunner
}

suspend fun computeJobResult(jobRequest: JobRequest, block: suspend (JobRequest) -> String): JobResult {
    var responseBody = ""
    var status: String
    var requestDurationMillis: Long

    try {
        requestDurationMillis = measureRealtimeMillis {
            responseBody = block(jobRequest)
        }

        status = calculateJobStatus(requestDurationMillis, jobRequest)
    } catch (e: IOException) {
        requestDurationMillis = 0L
        responseBody = e.message ?: ""
        status = Status.UNKNOWN
    }

    return JobResult(
        jobUuid = jobRequest.jobUuid,
        responseTime = requestDurationMillis,
        responseBody = responseBody.take(BODY_LENGTH_BYTES_MAX),
        status = status
    )
}

inline fun measureRealtimeMillis(block: () -> Unit): Long {
    val start = SystemClock.elapsedRealtime()
    block()
    return SystemClock.elapsedRealtime() - start
}

fun calculateJobStatus(requestDurationMillis: Long, jobRequest: JobRequest): String {
    val degradedAfterMillis = jobRequest.degradedAfter ?: DEGRADED_TIMEOUT_MILLIS
    val criticalAfterMillis = jobRequest.criticalAfter ?: CRITICAL_TIMEOUT_MILLIS

    return when {
        requestDurationMillis > degradedAfterMillis -> Status.DEGRADED
        requestDurationMillis > criticalAfterMillis -> Status.CRITICAL
        else -> Status.OK
    }
}

val JobRequest.endpointHost: String
    get() {
        endpointAddress ?: throw IOException("Missing endpoint address in $this")
        val regex = "^\\w+://".toRegex(RegexOption.IGNORE_CASE)
        return endpointAddress.replaceFirst(regex, "").replaceAfter('/', "")
    }

fun JobRequest.endpointPortOrDefault(default: Int): Int {
    return (endpointPort ?: default).coerceIn(TCP_UDP_PORT_RANGE)
}