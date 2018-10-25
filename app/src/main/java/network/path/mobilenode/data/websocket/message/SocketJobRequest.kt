package network.path.mobilenode.data.websocket.message

import network.path.mobilenode.domain.entity.JobRequest

class SocketJobRequest(
    override val id: String?,
    override val type: String?,
    protocol: String?,
    method: String?,
    headers: List<Map<String, String>>?,
    payload: String?,
    endpointAddress: String?,
    endpointPort: Int?,
    endpointAdditionalParams: String?,
    degradedAfter: Long?,
    criticalAfter: Long?,
    val jobUuid: String
) : JobRequest(protocol,
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
        jobUuid), PathMessage