package pl.droidsonroids.minertest.runner

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import pl.droidsonroids.minertest.message.JobRequest
import pl.droidsonroids.minertest.message.Method
import java.io.IOException

class HttpRunner : JobRunner {
    private val httpProtocolRegex = "^https?://.*".toRegex(RegexOption.IGNORE_CASE)
    private val client = OkHttpClient()

    override fun runJob(jobRequest: JobRequest) = computeJobResult(jobRequest, ::runHttpJob)

    private fun runHttpJob(jobRequest: JobRequest): String {
        val request = buildRequest(jobRequest)
        client.newCall(request).execute().use {
            return it.body()?.string() ?: ""
        }
    }

    private fun buildRequest(jobRequest: JobRequest): Request {
        val completeUrl = with(jobRequest) {
            val prependedProtocol = when {
                endpointAddress.matches(httpProtocolRegex) -> ""
                else -> "http://"
            }

            val urlPrefix = HttpUrl.parse("$prependedProtocol$endpointAddress") ?: throw IOException("Unparsable url: $endpointAddress")
            val urlPrefixWithPortBuilder = urlPrefix.newBuilder()
            if (endpointPort != null) {
                urlPrefixWithPortBuilder.port(endpointPort)
            }

            "${urlPrefixWithPortBuilder.build()}${endpointAdditionalParams.orEmpty()}"
        }

        val method = jobRequest.method ?: Method.GET
        val requestBuilder = Request.Builder()
            .method(method.name, null)
            .url(completeUrl)

        jobRequest.headers?.flatMap { it.entries }?.forEach { entry ->
            requestBuilder.addHeader(entry.key, entry.value)
        }

        return requestBuilder.build()
    }
}