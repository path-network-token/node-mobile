package pl.droidsonroids.minertest.runner

import okhttp3.HttpUrl
import okhttp3.Request
import pl.droidsonroids.minertest.message.JobRequest
import pl.droidsonroids.minertest.service.OkHttpClientFactory
import java.io.IOException

private val httpProtocolRegex = "^https?://.*".toRegex(RegexOption.IGNORE_CASE)

class HttpRunner : Runner {

    override suspend fun runJob(jobRequest: JobRequest) = computeJobResult(jobRequest) { runHttpJob(it) }

    private fun runHttpJob(jobRequest: JobRequest): String {
        val request = buildRequest(jobRequest)
        val client = OkHttpClientFactory.create()
        client.newCall(request).execute().use {
            return it.body()?.string().orEmpty()
        }
    }

    private fun buildRequest(jobRequest: JobRequest): Request {
        val completeUrl = with(jobRequest) {
            val prependedProtocol = when {
                endpointAddress == null -> throw IOException("Missing endpint address in $jobRequest")
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

        val method = jobRequest.method ?: "GET"
        val requestBuilder = Request.Builder()
            .method(method, null)
            .url(completeUrl)

        jobRequest.headers?.flatMap { it.entries }?.forEach { entry ->
            requestBuilder.addHeader(entry.key, entry.value)
        }

        return requestBuilder.build()
    }
}