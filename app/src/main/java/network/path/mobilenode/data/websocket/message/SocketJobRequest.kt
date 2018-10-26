package network.path.mobilenode.data.websocket.message

import network.path.mobilenode.domain.entity.JobRequest

data class SocketJobRequest(
        override val id: String?,
        override val type: String,
        val protocol: String?,
        val method: String?,
        val headers: List<Map<String, String>>?,
        val payload: String?,
        val endpointAddress: String?,
        val endpointPort: Int?,
        val endpointAdditionalParams: String?,
        val degradedAfter: Long?,
        val criticalAfter: Long?,
        val jobUuid: String
) : PathMessage {
    fun jobRequest() = JobRequest(type,
            protocol,
            method,
            headers,
            payload,
            endpointAddress,
            endpointPort,
            endpointAdditionalParams,
            degradedAfter,
            criticalAfter,
            emptyList(),
            emptyList(),
            jobUuid,
            jobUuid)
}
