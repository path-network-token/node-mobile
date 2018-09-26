package network.path.mobilenode.runner

import kotlinx.coroutines.experimental.withTimeout
import network.path.mobilenode.message.JobRequest
import pl.droidsonroids.tracepath.android.Tracepath
import java.util.concurrent.TimeUnit

class TracepathRunner : Runner {
    override suspend fun runJob(jobRequest: JobRequest) = computeJobResult(jobRequest) { runTracepathJob(it) }

    private suspend fun runTracepathJob(jobRequest: JobRequest) = withTimeout(time = 1, unit = TimeUnit.MINUTES) {
        Tracepath.tracepath(jobRequest.endpointHost)
    }
}