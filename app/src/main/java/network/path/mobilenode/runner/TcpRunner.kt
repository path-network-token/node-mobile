package network.path.mobilenode.runner

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.withTimeout
import network.path.mobilenode.Constants.DEFAULT_TCP_PORT
import network.path.mobilenode.Constants.JOB_TIMEOUT_MILLIS
import network.path.mobilenode.Constants.RESPONSE_LENGTH_BYTES_MAX
import network.path.mobilenode.Constants.TCP_UDP_READ_WRITE_TIMEOUT_MILLIS
import network.path.mobilenode.message.JobRequest
import java.net.InetSocketAddress
import javax.net.SocketFactory

class TcpRunner : Runner {

    override val checkType = CheckType.Tcp

    override suspend fun runJob(jobRequest: JobRequest) = computeJobResult(jobRequest) { runTcpJob(it) }

    private suspend fun runTcpJob(jobRequest: JobRequest): String = withTimeout(JOB_TIMEOUT_MILLIS) {
        SocketFactory.getDefault().createSocket().use {
            val port = jobRequest.endpointPortOrDefault(DEFAULT_TCP_PORT)
            val address = InetSocketAddress(jobRequest.endpointHost, port)

            it.connect(address, JOB_TIMEOUT_MILLIS.toInt())

            if (jobRequest.payload != null) {
                it.soTimeout = TCP_UDP_READ_WRITE_TIMEOUT_MILLIS.toInt()

                val response = async { it.readText(RESPONSE_LENGTH_BYTES_MAX) }
                writeTextToSocket(it, jobRequest.payload)

                return@use response.await()
            }
            "TCP connection established successfully"
        }
    }
}