package network.path.mobilenode.message

import network.path.mobilenode.json.MessageType

data class Ack(
    override val id: String?,
    override val type: String? = MessageType.ACK,
    val minerId: String? = null
) : MinerMessage