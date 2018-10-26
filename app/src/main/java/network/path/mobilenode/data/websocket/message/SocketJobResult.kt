package network.path.mobilenode.data.websocket.message

import network.path.mobilenode.data.json.MessageType
import network.path.mobilenode.domain.entity.CheckType
import network.path.mobilenode.domain.entity.JobResult

data class SocketJobResult(
        override val id: String = randomId(),
        override val type: String = MessageType.JOB_RESULT,
        val checkType: CheckType,
        val jobUuid: String,
        val status: String,
        val responseTime: Long,
        val responseBody: String
) : PathMessage {
    constructor(result: JobResult) : this(
            checkType = result.checkType,
            jobUuid = result.executionUuid,
            status = result.status,
            responseTime = result.responseTime,
            responseBody = result.responseBody)
}
