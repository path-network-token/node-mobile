package pl.droidsonroids.minertest.runner

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import pl.droidsonroids.minertest.Constants
import pl.droidsonroids.minertest.message.JobRequest
import java.net.InetSocketAddress
import java.net.Socket
import javax.net.SocketFactory

private const val DEFAULT_TCP_PORT = 80

class TcpRunner : Runner {

    override suspend fun runJob(jobRequest: JobRequest) = computeJobResult(jobRequest) { runTcpJob(it) }

    private suspend fun runTcpJob(jobRequest: JobRequest): String {
        SocketFactory.getDefault().createSocket().use {
            val port = jobRequest.endpointPortOrDefault(DEFAULT_TCP_PORT)
            it.connect(InetSocketAddress(jobRequest.endpointHost, port), Constants.TIMEOUT_MILLIS.toInt())
            it.soTimeout = Constants.TIMEOUT_MILLIS.toInt()

            val response = async { it.readText() }

            val payload = jobRequest.payload.orEmpty()
            it.writeText(payload)

            return response.await()
        }
    }

    private fun Socket.readText(): String {
        return getInputStream().bufferedReader().readText()
    }

    private suspend fun Socket.writeText(payload: String) {
        val writeJob = launch {
            getOutputStream().bufferedWriter().apply {
                write(payload)
                flush()
            }
        }
        writeJob.join()
    }
}