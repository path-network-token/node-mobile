package network.path.mobilenode.data.runner

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.withTimeout
import network.path.mobilenode.Constants.DEFAULT_TCP_PORT
import network.path.mobilenode.Constants.JOB_TIMEOUT_MILLIS
import network.path.mobilenode.Constants.RESPONSE_LENGTH_BYTES_MAX
import network.path.mobilenode.Constants.TCP_UDP_READ_WRITE_TIMEOUT_MILLIS
import network.path.mobilenode.domain.entity.CheckType
import network.path.mobilenode.domain.entity.JobRequest
import network.path.mobilenode.domain.entity.endpointHost
import network.path.mobilenode.domain.entity.endpointPortOrDefault
import java.net.InetSocketAddress
import javax.net.SocketFactory

class TcpRunner : Runner {

    override val checkType = CheckType.TCP

    override suspend fun runJob(jobRequest: JobRequest) = computeJobResult(checkType, jobRequest) { runTcpJob(it) }

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