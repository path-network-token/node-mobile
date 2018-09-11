package network.path.mobilenode.message

import network.path.mobilenode.json.MessageType

data class PathError(
    override val id: String?,
    override val type: String? = MessageType.ERROR,
    val description: String?
) : PathMessage