package pl.droidsonroids.minertest.message

data class Ack(
    override val id: String,
    override val type: MessageType = MessageType.ack,
    val minerId: String? = null
) : MinerMessage