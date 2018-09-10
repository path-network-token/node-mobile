package pl.droidsonroids.minertest.runner

import pl.droidsonroids.minertest.message.JobRequest
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

private const val DEFAULT_UDP_PORT = 67

class UdpRunner : Runner {
    override suspend fun runJob(jobRequest: JobRequest) = computeJobResult(jobRequest) { runUdpJob(it) }

    private fun runUdpJob(jobRequest: JobRequest): String {
        val port = jobRequest.endpointPortOrDefault(DEFAULT_UDP_PORT)

        DatagramSocket().use {
            val endpointInetAddress = InetAddress.getByName(jobRequest.endpointHost)
            val body = jobRequest.payload.orEmpty()

            val datagramPacket = DatagramPacket(body.toByteArray(), body.length, endpointInetAddress, port)
            it.send(datagramPacket)
        }

        return ""
    }
}