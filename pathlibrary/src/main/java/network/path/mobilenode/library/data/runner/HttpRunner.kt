package network.path.mobilenode.library.data.runner

import kotlinx.coroutines.InternalCoroutinesApi
import network.path.mobilenode.library.BuildConfig
import network.path.mobilenode.library.Constants
import network.path.mobilenode.library.data.http.OkHttpWorkerPool
import network.path.mobilenode.library.data.http.getBody
import network.path.mobilenode.library.domain.PathStorage
import network.path.mobilenode.library.domain.entity.CheckType
import network.path.mobilenode.library.domain.entity.JobRequest
import okhttp3.HttpUrl
import okhttp3.Request
import java.io.IOException

@InternalCoroutinesApi
class HttpRunner(private val workerPool: OkHttpWorkerPool, private val storage: PathStorage) : Runner {
    companion object {
        private val HTTP_PROTOCOL_REGEX = "^https?://.*".toRegex(RegexOption.IGNORE_CASE)
    }

    override val checkType = CheckType.HTTP

    override suspend fun runJob(jobRequest: JobRequest) = computeJobResult(checkType, jobRequest) { runHttpJob(it) }

    private suspend fun runHttpJob(jobRequest: JobRequest): String {
        val request = buildRequest(jobRequest)

        val response = workerPool.execute(request)
        val body = response.getBody()
        return body.string()
    }

    private fun buildRequest(jobRequest: JobRequest): Request {
        val completeUrl = with(jobRequest) {
            val prependedProtocol = when {
                endpointAddress == null -> throw IOException("Missing endpoint address in $jobRequest")
                endpointAddress.matches(HTTP_PROTOCOL_REGEX) -> ""
                else -> "http://"
            }

            val urlPrefix = HttpUrl.parse("$prependedProtocol$endpointAddress")
                    ?: throw IOException("Unparsable url: $endpointAddress")
            val urlPrefixWithPortBuilder = urlPrefix.newBuilder()
            if (endpointPort != null && Constants.TCP_UDP_PORT_RANGE.contains(endpointPort)) {
                urlPrefixWithPortBuilder.port(endpointPort)
            }

            "${urlPrefixWithPortBuilder.build()}${endpointAdditionalParams.orEmpty()}"
        }

        val method = jobRequest.method ?: "GET"
        val requestBuilder = Request.Builder()
                .method(method, null)
                .url(completeUrl)

        var hasUserAgent = false
        jobRequest.headers?.flatMap { it.entries }?.forEach { entry ->
            requestBuilder.addHeader(entry.key, entry.value)
            if (entry.key == "User-Agent") {
                hasUserAgent = true
            }
        }

        if (!hasUserAgent) {
            val nodeId = storage.nodeId
            val ua = "Mozilla/5.0 (Path Network ${Constants.PATH_API_VERSION}; Android; ${System.getProperty("os.arch")}) ${BuildConfig.VERSION_NAME}/${BuildConfig.VERSION_CODE} (KHTML, like Gecko) Node/$nodeId"
            requestBuilder.addHeader("User-Agent", ua)
        }

        return requestBuilder.build()
    }
}