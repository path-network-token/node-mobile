package network.path.mobilenode.data.runner

import android.os.SystemClock
import com.crashlytics.android.Crashlytics
import network.path.mobilenode.Constants.DEFAULT_CRITICAL_TIMEOUT_MILLIS
import network.path.mobilenode.Constants.DEFAULT_DEGRADED_TIMEOUT_MILLIS
import network.path.mobilenode.domain.entity.Status
import network.path.mobilenode.domain.entity.CheckType
import network.path.mobilenode.domain.entity.JobRequest
import network.path.mobilenode.domain.entity.JobResult
import timber.log.Timber
import java.io.IOException

suspend fun computeJobResult(checkType: CheckType, jobRequest: JobRequest, block: suspend (JobRequest) -> String): JobResult {
    var responseBody = ""
    var isResponseKnown = false

    val requestDurationMillis = measureRealtimeMillis {
        try {
            responseBody = block(jobRequest)
            isResponseKnown = true
        } catch (e: IOException) {
            responseBody = e.toString()
        } catch (e: Exception) {
            responseBody = e.toString()
            Crashlytics.logException(e)
        }
    }

    val status = when (isResponseKnown) {
        true -> calculateJobStatus(requestDurationMillis, jobRequest)
        false -> Status.UNKNOWN
    }

    Timber.d("RUNNER: [$jobRequest] => $status")
    return JobResult(
            checkType = checkType,
            executionUuid = jobRequest.executionUuid,
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

fun calculateJobStatus(requestDurationMillis: Long, jobRequest: JobRequest): String {
    val degradedAfterMillis = jobRequest.degradedAfter ?: DEFAULT_DEGRADED_TIMEOUT_MILLIS
    val criticalAfterMillis = jobRequest.criticalAfter ?: DEFAULT_CRITICAL_TIMEOUT_MILLIS

    return when {
        requestDurationMillis > degradedAfterMillis -> Status.DEGRADED
        requestDurationMillis > criticalAfterMillis -> Status.CRITICAL
        else -> Status.OK
    }
}
