package network.path.mobilenode.domain.entity

import network.path.mobilenode.Constants
import java.io.IOException

data class JobRequest(
        val type: String = "job-request",
        val protocol: String?,
        val method: String?,
        val headers: List<Map<String, String>>?,
        val payload: String?,
        val endpointAddress: String?,
        val endpointPort: Int?,
        val endpointAdditionalParams: String?,
        val degradedAfter: Long?,
        val criticalAfter: Long?,
        val criticalResponses: List<Map<String, Any>>,
        val validResponses: List<Map<String, Any>>,
        val jobUuid: String,
        var executionUuid: String
)

val JobRequest.endpointHost: String
    get() {
        endpointAddress ?: throw IOException("Missing endpoint address in $this")
        val regex = "^\\w+://".toRegex(RegexOption.IGNORE_CASE)
        return endpointAddress.replaceFirst(regex, "").replaceAfter('/', "")
    }

fun JobRequest.endpointPortOrDefault(default: Int): Int =
        (endpointPort ?: default).coerceIn(Constants.TCP_UDP_PORT_RANGE)
