package network.path.mobilenode.runner

import java.io.ByteArrayOutputStream
import java.net.Socket

fun Socket.readText(maxSize: Int): String {
    ByteArrayOutputStream(maxSize).use {
        getInputStream().copyTo(it)
        return String(it.toByteArray())
    }
}

fun writeTextToSocket(socket: Socket, payload: String) {
    socket.getOutputStream().bufferedWriter().apply {
        write(payload)
        flush()
    }
}