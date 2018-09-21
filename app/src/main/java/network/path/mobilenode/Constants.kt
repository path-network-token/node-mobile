package network.path.mobilenode

object Constants {
    const val TIMEOUT_MILLIS = 10_000L
    const val TCP_READ_WRITE_TIMEOUT_MILLIS = 5_000
    val TCP_UDP_PORT_RANGE = 1..0xFFFF
    const val RESPONSE_LENGTH_BYTES_MAX = 1 shl 15
}