package network.path.mobilenode.runner

import kotlinx.coroutines.experimental.withTimeout
import network.path.mobilenode.Constants
import network.path.mobilenode.Constants.DEFAULT_TRACEPATH_PORT
import network.path.mobilenode.message.JobRequest
import pl.droidsonroids.tracepath.android.Tracepath

class TracepathRunner : Runner {
    override suspend fun runJob(jobRequest: JobRequest) = computeJobResult(jobRequest) { runTracepathJob(it) }

    private suspend fun runTracepathJob(jobRequest: JobRequest) = withTimeout(Constants.TRACEPATH_JOB_TIMEOUT_MILLIS) {
        val port = jobRequest.endpointPortOrDefault(DEFAULT_TRACEPATH_PORT)
        Tracepath.tracepath(jobRequest.endpointHost, port)
    }
}