package network.path.mobilenode.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import network.path.mobilenode.library.domain.PathSystem
import network.path.mobilenode.library.domain.entity.ConnectionStatus
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class MainViewModel(private val pathSystem: PathSystem) : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private val _isLooking = MutableLiveData<Boolean>()
    val isLooking: LiveData<Boolean> = _isLooking

    private var job: Job? = null

    fun onViewCreated() {
        job = launch {
            pathSystem.status.consumeEach {
                val oldValue = _isLooking.value
                if (oldValue == false) {
                    job?.cancel()
                } else {
                    _isLooking.postValue(it == ConnectionStatus.LOOKING)
                }
            }
        }
    }

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }
}
