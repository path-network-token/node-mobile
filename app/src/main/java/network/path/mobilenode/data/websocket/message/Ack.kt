package network.path.mobilenode.data.websocket.message

import network.path.mobilenode.data.json.MessageType

data class Ack(
    override val id: String?,
    override val type: String? = MessageType.ACK,
    val nodeId: String? = null
) : PathMessage