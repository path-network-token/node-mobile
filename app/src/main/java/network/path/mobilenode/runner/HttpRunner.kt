package network.path.mobilenode.runner

import okhttp3.HttpUrl
import okhttp3.Request
import network.path.mobilenode.Constants.TCP_UDP_PORT_RANGE
import network.path.mobilenode.message.JobRequest
import network.path.mobilenode.service.OkHttpClientFactory
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
            if (endpointPort != null && TCP_UDP_PORT_RANGE.contains(endpointPort)) {
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