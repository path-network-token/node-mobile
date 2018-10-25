package network.path.mobilenode.data.websocket.message

import java.util.*

interface PathMessage {
    val id: String?
    val type: String?
}

fun randomId() = UUID.randomUUID().toString()