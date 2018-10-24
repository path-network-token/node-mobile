package network.path.mobilenode.data.storage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import network.path.mobilenode.data.runner.CheckType
import network.path.mobilenode.domain.entity.ConnectionStatus
import network.path.mobilenode.domain.entity.message.JobResult

class PathRepository(private val storage: Storage) {

    private val _connectionStatus = MutableLiveData<ConnectionStatus>()
    val connectionStatus: LiveData<ConnectionStatus> = _connectionStatus

    private val _nodeId = MutableLiveData<String?>().also { it.postValue(storage.nodeId) }
    val nodeId: LiveData<String?> = _nodeId

    var nodeIdString
        get() = storage.nodeId
        set(value) {
            if (storage.nodeId == null) {
                storage.nodeId = value
                _nodeId.postValue(value)
            }
        }

    val pathWalletAddress
        get() = storage.pathWalletAddress

    fun postConnectionStatus(connectionStatus: ConnectionStatus) {
        _connectionStatus.postValue(connectionStatus)
    }

    fun recordJobResult(checkType: CheckType, jobResult: JobResult) {
        val stats = storage.checkStatistics[checkType]
        val latencySum = stats.count * stats.averageLatencyMillis

        val newCount = stats.count + 1
        val newLatencySum = latencySum + jobResult.responseTime
        val newAverageLatency = newLatencySum / newCount
        storage.checkStatistics[checkType] = CheckTypeStatistics(newCount, newAverageLatency)
    }

    fun getCheckStatistics(checkType: CheckType) = storage.checkStatistics[checkType]
}