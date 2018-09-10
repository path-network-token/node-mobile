package pl.droidsonroids.minertest.message

import java.util.*

interface MinerMessage {
    val id: String?
    val type: String?
}

fun randomId() = UUID.randomUUID().toString()