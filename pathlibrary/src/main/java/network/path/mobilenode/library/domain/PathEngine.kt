package network.path.mobilenode.library.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import network.path.mobilenode.library.domain.entity.ConnectionStatus
import network.path.mobilenode.library.domain.entity.JobList
import network.path.mobilenode.library.domain.entity.JobRequest
import network.path.mobilenode.library.domain.entity.JobResult

@ExperimentalCoroutinesApi
interface PathEngine {
    val status: BroadcastChannel<ConnectionStatus>
    val requests: BroadcastChannel<JobRequest>
    val nodeId: BroadcastChannel<String?>
    val jobList: BroadcastChannel<JobList>
    val isRunning: BroadcastChannel<Boolean>

    // Initializes connection and starts retrieving (by either listening or polling) jobs
    fun start()

    // Send result of a job back to server
    fun processResult(result: JobResult)

    // Stop any interaction with the server
    fun stop()

    fun toggle()
}
