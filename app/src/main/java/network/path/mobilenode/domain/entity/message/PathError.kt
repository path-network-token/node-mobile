package network.path.mobilenode.domain.entity.message

import network.path.mobilenode.data.json.MessageType

data class PathError(
    override val id: String?,
    override val type: String? = MessageType.ERROR,
    val description: String?
) : PathMessage