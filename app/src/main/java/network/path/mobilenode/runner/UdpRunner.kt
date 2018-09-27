package network.path.mobilenode.runner

import kotlinx.coroutines.experimental.withTimeout
import network.path.mobilenode.Constants.DEFAULT_UDP_PORT
import network.path.mobilenode.Constants.JOB_TIMEOUT_MILLIS
import network.path.mobilenode.message.JobRequest
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class UdpRunner : Runner {
    override suspend fun runJob(jobRequest: JobRequest) = computeJobResult(jobRequest) { runUdpJob(it) }

    private suspend fun runUdpJob(jobRequest: JobRequest) = withTimeout(JOB_TIMEOUT_MILLIS) {
        val port = jobRequest.endpointPortOrDefault(DEFAULT_UDP_PORT)

        DatagramSocket().use {
            val socketAddress = InetAddress.getByName(jobRequest.endpointHost)
            val body = jobRequest.payload.orEmpty()

            val datagramPacket = DatagramPacket(body.toByteArray(), body.length, socketAddress, port)
            it.send(datagramPacket)
        }
        "UDP packet sent successfully"
    }
}