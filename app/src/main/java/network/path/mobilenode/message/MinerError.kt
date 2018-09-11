package network.path.mobilenode.message

import network.path.mobilenode.json.MessageType

data class MinerError(
    override val id: String?,
    override val type: String? = MessageType.ERROR,
    val description: String?
) : MinerMessage