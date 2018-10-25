package network.path.mobilenode.data.websocket.message

import network.path.mobilenode.BuildConfig
import network.path.mobilenode.data.json.MessageType
import network.path.mobilenode.domain.entity.CheckIn

class SocketCheckIn(
        override val id: String = randomId(),
        override val type: String = MessageType.CHECK_IN,
        nodeId: String?,
        lat: String? = null,
        lon: String? = null,
        wallet: String,
        deviceType: String? = "android",
        pathApiVersion: String = "1.0",
        nodeBuildVersion: String = BuildConfig.VERSION_NAME
) : CheckIn(type, nodeId, lat, lon, wallet, deviceType, pathApiVersion, nodeBuildVersion), PathMessage