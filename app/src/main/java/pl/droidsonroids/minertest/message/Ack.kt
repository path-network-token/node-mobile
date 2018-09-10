package pl.droidsonroids.minertest.message

import pl.droidsonroids.minertest.json.MessageType

data class Ack(
    override val id: String?,
    override val type: String? = MessageType.ACK,
    val minerId: String? = null
) : MinerMessage