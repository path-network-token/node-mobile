package network.path.mobilenode.message

import network.path.mobilenode.json.MessageType

data class JobResult(
    override val id: String = randomId(),
    override val type: String = MessageType.JOB_RESULT,
    val jobUuid: String,
    val status: String,
    val responseTime: Long,
    val responseBody: String
) : PathMessage