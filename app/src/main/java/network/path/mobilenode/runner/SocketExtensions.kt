package network.path.mobilenode.runner

import kotlinx.coroutines.experimental.launch
import java.io.ByteArrayOutputStream
import java.net.Socket

fun Socket.readText(maxSize: Int): String {
    val buffer = ByteArrayOutputStream(maxSize)
    getInputStream().copyTo(buffer)
    return String(buffer.toByteArray())
}

suspend fun Socket.writeText(payload: String) {
    val writeJob = launch {
        getOutputStream().bufferedWriter().apply {
            write(payload)
            flush()
        }
    }
    writeJob.join()
}