package pl.droidsonroids.minertest.runner

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import pl.droidsonroids.minertest.message.JobRequest
import java.io.IOException

class HttpRunner : JobRunner {
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