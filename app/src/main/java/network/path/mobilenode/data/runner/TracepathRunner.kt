package network.path.mobilenode.data.runner

import kotlinx.coroutines.experimental.withTimeout
import network.path.mobilenode.Constants
import network.path.mobilenode.Constants.DEFAULT_TRACEPATH_PORT
import network.path.mobilenode.domain.entity.CheckType
import network.path.mobilenode.domain.entity.JobRequest
import network.path.mobilenode.domain.entity.endpointHost
import network.path.mobilenode.domain.entity.endpointPortOrDefault
import pl.droidsonroids.tracepath.android.Tracepath

class TracepathRunner : Runner {

    override val checkType = CheckType.TRACEROUTE

    override suspend fun runJob(jobRequest: JobRequest) = computeJobResult(checkType, jobRequest) { runTracepathJob(it) }

    private suspend fun runTracepathJob(jobRequest: JobRequest) =
            withTimeout(Constants.TRACEPATH_JOB_TIMEOUT_MILLIS) {
                val port = jobRequest.endpointPortOrDefault(DEFAULT_TRACEPATH_PORT)
                Tracepath.tracepath(jobRequest.endpointHost, port)
            }
}