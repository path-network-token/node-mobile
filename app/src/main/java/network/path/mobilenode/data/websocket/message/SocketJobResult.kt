package network.path.mobilenode.data.websocket.message

import network.path.mobilenode.data.json.MessageType
import network.path.mobilenode.domain.entity.CheckType
import network.path.mobilenode.domain.entity.JobResult

class SocketJobResult(
        override val id: String = randomId(),
        override val type: String = MessageType.JOB_RESULT,
        checkType: CheckType,
        jobUuid: String,
        status: String,
        responseTime: Long,
        responseBody: String
) : JobResult(
        checkType,
        jobUuid,
        status,
        responseTime,
        responseBody,
        responseBody.length),
        PathMessage {

    constructor(result: JobResult) : this(
            checkType = result.checkType,
            jobUuid = result.executionUuid,
            status = result.status,
            responseTime = result.responseTime,
            responseBody = result.responseBody)
}