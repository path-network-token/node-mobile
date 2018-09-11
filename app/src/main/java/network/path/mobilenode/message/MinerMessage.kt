package network.path.mobilenode.message

import java.util.*

interface MinerMessage {
    val id: String?
    val type: String?
}

fun randomId() = UUID.randomUUID().toString()