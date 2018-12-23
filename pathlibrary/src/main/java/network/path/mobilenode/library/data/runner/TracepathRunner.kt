package network.path.mobilenode.library.data.runner

import com.google.gson.Gson
import kotlinx.coroutines.withTimeout
import network.path.mobilenode.library.Constants
import network.path.mobilenode.library.data.runner.mrt.MTR
import network.path.mobilenode.library.domain.entity.CheckType
import network.path.mobilenode.library.domain.entity.JobRequest
import network.path.mobilenode.library.domain.entity.endpointHost

class TracepathRunner(private val gson: Gson) : Runner {
    companion object {
        init {
            System.loadLibrary("traceroute")
        }
    }

    override val checkType = CheckType.TRACEROUTE

    override suspend fun runJob(jobRequest: JobRequest) = computeJobResult(checkType, jobRequest) { runTracepathJob(it) }

    private suspend fun runTracepathJob(jobRequest: JobRequest) =
            withTimeout(Constants.TRACEPATH_JOB_TIMEOUT_MILLIS) {
                val port = jobRequest.endpointPort ?: 0
                val res = MTR().trace(jobRequest.endpointHost, port)
                if (res != null) gson.toJson(res.filter { it != null && it.ttl != 0 }) else ""
            }
}
