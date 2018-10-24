package network.path.mobilenode.domain.entity.message

import java.util.*

interface PathMessage {
    val id: String?
    val type: String?
}

fun randomId() = UUID.randomUUID().toString()