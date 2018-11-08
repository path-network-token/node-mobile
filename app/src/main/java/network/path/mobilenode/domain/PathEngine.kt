package network.path.mobilenode.domain

import kotlinx.coroutines.experimental.channels.BroadcastChannel
import network.path.mobilenode.domain.entity.ConnectionStatus
import network.path.mobilenode.domain.entity.JobList
import network.path.mobilenode.domain.entity.JobRequest
import network.path.mobilenode.domain.entity.JobResult

interface PathEngine {
    val status: BroadcastChannel<ConnectionStatus>
    val requests: BroadcastChannel<JobRequest>
    val nodeId: BroadcastChannel<String?>
    val jobList: BroadcastChannel<JobList>

    // Initializes connection and starts retrieving (by either listening or polling) jobs
    fun start()

    // Send result of a job back to server
    fun processResult(result: JobResult)

    // Stop any interaction with the server
    fun stop()
}
