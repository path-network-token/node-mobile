package pl.droidsonroids.minertest.message

data class CheckIn(
    override val id: String = randomId(),
    override val type: MessageType = MessageType.`check-in`,
    val lat: String? = null,
    val lon: String? = null,
    val minerId: String?,
    val wallet: String,
    val deviceType: String? = "android"
) : MinerMessage