package pl.droidsonroids.minertest.message

data class JobResult(
    override val id: String = randomId(),
    override val type: MessageType = MessageType.`job-result`,
    val jobUuid: String,
    val status: Status,
    val responseTime: Long,
    val responseBody: String
) : MinerMessage