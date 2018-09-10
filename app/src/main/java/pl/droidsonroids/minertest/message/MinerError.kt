package pl.droidsonroids.minertest.message

import pl.droidsonroids.minertest.json.MessageType

data class MinerError(
    override val id: String?,
    override val type: String? = MessageType.ERROR,
    val description: String?
) : MinerMessage