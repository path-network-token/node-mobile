package network.path.mobilenode.data.websocket.message

import network.path.mobilenode.data.json.MessageType

data class PathError(
    override val id: String?,
    override val type: String? = MessageType.ERROR,
    val description: String?
) : PathMessage