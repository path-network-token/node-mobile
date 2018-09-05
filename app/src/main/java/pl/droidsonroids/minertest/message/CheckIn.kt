package pl.droidsonroids.minertest.message

data class CheckIn(
    override val id: String = randomId(),
    override val type: MessageType = MessageType.`check-in`,
    val minerId: String?,
    val wallet: String,
    val deviceType: Device = Device.android
) : MinerMessage