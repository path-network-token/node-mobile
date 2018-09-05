package pl.droidsonroids.minertest.message

import java.util.*

interface MinerMessage {
    val id: String
    val type: MessageType
}

fun randomId() = UUID.randomUUID().toString()