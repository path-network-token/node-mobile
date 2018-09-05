package pl.droidsonroids.minertest.runner

import android.os.SystemClock
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import pl.droidsonroids.minertest.message.JobRequest
import pl.droidsonroids.minertest.message.JobResult
import pl.droidsonroids.minertest.message.Status
import java.io.IOException

class HttpRunner : JobRunner {
    private val client = OkHttpClient()

    override fun runJob(jobRequest: JobRequest): JobResult {

        try {
            val request = buildRequest(jobRequest)
            val startTimeMillis = SystemClock.elapsedRealtime()

            client.newCall(request).execute().use {
                val endTimeMillis = SystemClock.elapsedRealtime()
                val requestDurationMillis = endTimeMillis - startTimeMillis

                val status = when {
                    requestDurationMillis > jobRequest.degradedAfter -> Status.degraded
                    requestDurationMillis > jobRequest.criticalAfter -> Status.critical
                    else -> Status.ok
                }

                return JobResult(
                    jobUuid = jobRequest.jobUuid,
                    responseTime = requestDurationMillis,
                    responseBody = it.body()?.string() ?: "",
                    status = status
                )
            }
        } catch (e: IOException) {
            return JobResult(
                jobUuid = jobRequest.jobUuid,
                responseTime = 0,
                responseBody = "",
                status = Status.unknown
            )
        }
    }

    private fun buildRequest(jobRequest: JobRequest): Request {
        val completeUrl = with(jobRequest) {
            val prependedProtocol = when {
                endpointAddress.startsWith("http://", true) || endpointAddress.startsWith("https://", true) -> ""
                else -> "http://"
            }
            val urlPrefix = HttpUrl.parse("$prependedProtocol$endpointAddress") ?: throw IOException("Unparsable url: $endpointAddress")
            val urlPrefixWithPort = urlPrefix.newBuilder()
                .port(endpointPort)
                .build()

            "$urlPrefixWithPort$endpointAdditionalParams"
        }

        val requestBuilder = Request.Builder()
            .method(jobRequest.method.name, null)
            .url(completeUrl)

        jobRequest.headers.flatMap { it.entries }.forEach { entry ->
            requestBuilder.addHeader(entry.key, entry.value)
        }

        return requestBuilder.build()
    }
}