package network.path.mobilenode.domain.entity

import network.path.mobilenode.Constants
import java.io.IOException

data class JobRequest(
        val type: String = "job-request",
        val protocol: String? = null,
        val method: String? = null,
        val headers: List<Map<String, String>>? = null,
        val payload: String? = null,
        val endpointAddress: String? = null,
        val endpointPort: Int? = null,
        val endpointAdditionalParams: String? = null,
        val degradedAfter: Long? = null,
        val criticalAfter: Long? = null,
        val criticalResponses: List<Map<String, Any>> = emptyList(),
        val validResponses: List<Map<String, Any>> = emptyList(),
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
