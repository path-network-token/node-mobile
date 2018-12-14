package network.path.mobilenode.data.runner

import com.google.gson.Gson
import network.path.mobilenode.domain.PathStorage
import network.path.mobilenode.domain.entity.JobRequest
import okhttp3.OkHttpClient

class Runners(private val okHttpClient: OkHttpClient, private val storage: PathStorage, private val gson: Gson) {
    operator fun get(jobRequest: JobRequest) = with(jobRequest) {
        when {
            protocol == null -> FallbackRunner
            protocol.startsWith(prefix = "http", ignoreCase = true) -> HttpRunner(okHttpClient, storage)
            protocol.startsWith(prefix = "tcp", ignoreCase = true) -> TcpRunner()
            protocol.startsWith(prefix = "udp", ignoreCase = true) -> UdpRunner()
            method.orEmpty().startsWith(prefix = "traceroute", ignoreCase = true) -> TracepathRunner(gson)
            else -> FallbackRunner
        }
    }
}