package network.path.mobilenode.runner

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.withTimeout
import network.path.mobilenode.Constants
import network.path.mobilenode.message.JobRequest
import java.net.InetSocketAddress
import javax.net.SocketFactory

private const val DEFAULT_TCP_PORT = 80

class TcpRunner : Runner {

    override suspend fun runJob(jobRequest: JobRequest) = computeJobResult(jobRequest) { runTcpJob(it) }

    private suspend fun runTcpJob(jobRequest: JobRequest): String = withTimeout(Constants.TIMEOUT_MILLIS) {
        SocketFactory.getDefault().createSocket().use {
            val port = jobRequest.endpointPortOrDefault(DEFAULT_TCP_PORT)
            it.connect(InetSocketAddress(jobRequest.endpointHost, port), Constants.TIMEOUT_MILLIS.toInt())

            if (jobRequest.payload != null) {
                it.soTimeout = Constants.TCP_READ_WRITE_TIMEOUT_MILLIS

                val response = async { it.readText(Constants.RESPONSE_LENGTH_BYTES_MAX) }

                writeTextToSocket(it, jobRequest.payload)
                response.await()
            }
            "TCP connection established successfully"
        }
    }
}