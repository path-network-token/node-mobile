package pl.droidsonroids.minertest.message

data class MinerError(
    override val id: String,
    override val type: MessageType = MessageType.error,
    val description: String
) : MinerMessage