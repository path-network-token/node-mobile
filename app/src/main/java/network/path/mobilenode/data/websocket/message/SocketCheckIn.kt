package network.path.mobilenode.data.websocket.message

import network.path.mobilenode.BuildConfig
import network.path.mobilenode.data.json.MessageType

data class SocketCheckIn(
        override val id: String = randomId(),
        override val type: String = MessageType.CHECK_IN,
        val nodeId: String?,
        val wallet: String,
        val lat: String?,
        val lon: String?,
        val deviceType: String? = "android",
        val pathApiVersion: String = "1.0",
        val nodeBuildVersion: String = BuildConfig.VERSION_NAME
) : PathMessage
