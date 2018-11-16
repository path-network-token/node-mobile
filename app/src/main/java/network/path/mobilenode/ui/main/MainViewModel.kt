package network.path.mobilenode.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.Main
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import network.path.mobilenode.domain.PathSystem
import network.path.mobilenode.domain.entity.ConnectionStatus
import kotlin.coroutines.experimental.CoroutineContext

class MainViewModel(private val system: PathSystem) : ViewModel(), CoroutineScope {
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val _status = MutableLiveData<ConnectionStatus>()
    val status: LiveData<ConnectionStatus> = _status

    private val _isRunning = MutableLiveData<Boolean>()
    val isRunning: LiveData<Boolean> = _isRunning

    fun onViewCreated() {
        registerStatusHandler()
        registerRunningHandler()
    }

    private fun registerStatusHandler() = launch {
        system.status.openSubscription().consumeEach {
            _status.postValue(it)
        }
    }

    private fun registerRunningHandler() = launch {
        system.isRunning.openSubscription().consumeEach {
            _isRunning.postValue(it)
        }
    }

    override fun onCleared() {
        job.cancel()
        super.onCleared()
    }
}
