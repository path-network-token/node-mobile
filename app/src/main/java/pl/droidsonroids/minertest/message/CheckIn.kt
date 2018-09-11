package pl.droidsonroids.minertest.message

import pl.droidsonroids.minertest.json.MessageType

data class CheckIn(
    override val id: String = randomId(),
    override val type: String = MessageType.CHECK_IN,
    val lat: String? = null,
    val lon: String? = null,
    val minerId: String?,
    val wallet: String,
    val deviceType: String? = "android"
) : MinerMessage