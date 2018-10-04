package network.path.mobilenode.storage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import network.path.mobilenode.model.ConnectionStatus

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
}