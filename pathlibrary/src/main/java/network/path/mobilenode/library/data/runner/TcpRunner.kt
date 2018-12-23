package network.path.mobilenode.library.data.runner

import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeout
import network.path.mobilenode.library.Constants
import network.path.mobilenode.library.domain.entity.CheckType
import network.path.mobilenode.library.domain.entity.JobRequest
import network.path.mobilenode.library.domain.entity.endpointHost
import network.path.mobilenode.library.domain.entity.endpointPortOrDefault
import network.path.mobilenode.library.utils.readText
import network.path.mobilenode.library.utils.writeText
import java.net.InetSocketAddress
import javax.net.SocketFactory

class TcpRunner : Runner {

    override val checkType = CheckType.TCP

    override suspend fun runJob(jobRequest: JobRequest) = computeJobResult(checkType, jobRequest) { runTcpJob(it) }

    private suspend fun runTcpJob(jobRequest: JobRequest): String = withTimeout(Constants.JOB_TIMEOUT_MILLIS) {
        SocketFactory.getDefault().createSocket().use {
            val port = jobRequest.endpointPortOrDefault(Constants.DEFAULT_TCP_PORT)
            val address = InetSocketAddress(jobRequest.endpointHost, port)

            it.connect(address, Constants.JOB_TIMEOUT_MILLIS.toInt())

            if (jobRequest.payload != null) {
                it.soTimeout = Constants.TCP_UDP_READ_WRITE_TIMEOUT_MILLIS.toInt()

                val response = async { it.readText(Constants.RESPONSE_LENGTH_BYTES_MAX) }
                it.writeText(jobRequest.payload)

                return@use response.await()
            }
            "TCP connection established successfully"
        }
    }
}