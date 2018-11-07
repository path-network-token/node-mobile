package network.path.mobilenode.data.runner

import java.io.ByteArrayOutputStream
import java.net.Socket

fun Socket.readText(maxSize: Int): String {
    ByteArrayOutputStream(maxSize).use {
        getInputStream().copyTo(it)
        return String(it.toByteArray())
    }
}

fun Socket.writeText(payload: String) {
    this.getOutputStream().bufferedWriter().apply {
        write(payload)
        flush()
    }
}