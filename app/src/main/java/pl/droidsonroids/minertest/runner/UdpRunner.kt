package pl.droidsonroids.minertest.runner

import kotlinx.coroutines.experimental.withTimeout
import pl.droidsonroids.minertest.Constants
import pl.droidsonroids.minertest.message.JobRequest
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress

private const val DEFAULT_UDP_PORT = 67

class UdpRunner : Runner {
    override suspend fun runJob(jobRequest: JobRequest) = computeJobResult(jobRequest) { runUdpJob(it) }

    private suspend fun runUdpJob(jobRequest: JobRequest): String = withTimeout(Constants.TIMEOUT_MILLIS) {
        val port = jobRequest.endpointPortOrDefault(DEFAULT_UDP_PORT)

        DatagramSocket().use {
            val socketAddress = InetSocketAddress(jobRequest.endpointHost, port)
            val body = jobRequest.payload.orEmpty()

            val datagramPacket = DatagramPacket(body.toByteArray(), body.length, socketAddress)
            it.send(datagramPacket)
        }
        ""
    }
}