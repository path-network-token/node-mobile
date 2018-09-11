package network.path.mobilenode.message

import network.path.mobilenode.json.MessageType

data class CheckIn(
    override val id: String = randomId(),
    override val type: String = MessageType.CHECK_IN,
    val lat: String? = null,
    val lon: String? = null,
    val minerId: String?,
    val wallet: String,
    val deviceType: String? = "android"
) : MinerMessage