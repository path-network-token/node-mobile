package network.path.mobilenode.runner

import network.path.mobilenode.message.JobRequest
import okhttp3.OkHttpClient

class Runners(private val okHttpClient: OkHttpClient) {

    operator fun get(jobRequest: JobRequest) = with(jobRequest) {
        when {
            protocol == null -> FallbackRunner
            protocol.startsWith(prefix = "http", ignoreCase = true) -> HttpRunner(okHttpClient)
            protocol.startsWith(prefix = "tcp", ignoreCase = true) -> TcpRunner()
            protocol.startsWith(prefix = "udp", ignoreCase = true) -> UdpRunner()
            method.orEmpty().startsWith(prefix = "traceroute", ignoreCase = true) -> TracepathRunner()
            else -> FallbackRunner
        }
    }
}