package pl.droidsonroids.minertest.message

import pl.droidsonroids.minertest.json.MessageType

data class JobResult(
    override val id: String = randomId(),
    override val type: String = MessageType.JOB_RESULT,
    val jobUuid: String,
    val status: String,
    val responseTime: Long,
    val responseBody: String
) : MinerMessage