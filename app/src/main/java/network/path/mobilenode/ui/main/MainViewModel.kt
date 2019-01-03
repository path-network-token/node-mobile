package network.path.mobilenode.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import network.path.mobilenode.library.domain.PathSystem
import network.path.mobilenode.library.domain.entity.ConnectionStatus

class MainViewModel(private val pathSystem: PathSystem) : ViewModel() {
    private val _isLooking = MutableLiveData<Boolean>()
    val isLooking: LiveData<Boolean> = _isLooking

    private val listener = object : PathSystem.BaseListener() {
        override fun onStatusChanged(status: ConnectionStatus) {
            updateStatus(status)
        }
    }

    fun onViewCreated() {
        pathSystem.addListener(listener)
        updateStatus(pathSystem.status)
    }

    override fun onCleared() {
        pathSystem.removeListener(listener)
        super.onCleared()
    }

    private fun updateStatus(status: ConnectionStatus) {
        val oldValue = _isLooking.value
        if (oldValue == false) {
            pathSystem.removeListener(listener)
        } else {
            _isLooking.postValue(status == ConnectionStatus.LOOKING)
        }
    }
}
