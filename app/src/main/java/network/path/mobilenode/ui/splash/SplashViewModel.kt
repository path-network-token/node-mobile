package network.path.mobilenode.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.Main
import network.path.mobilenode.storage.Storage
import kotlin.coroutines.experimental.CoroutineContext

typealias ShowScreen = Unit

private const val SPLASH_SHOW_TIME_MILLIS = 2000L

class SplashViewModel(private val storage: Storage) : ViewModel(), CoroutineScope {

    private val _showIntroScreen = MutableLiveData<ShowScreen>()
    val showIntroScreen: LiveData<ShowScreen> = _showIntroScreen

    private val _showMainScreen = MutableLiveData<ShowScreen>()
    val showMainScreen: LiveData<ShowScreen> = _showMainScreen

    private val showNextScreenJob = launch(start = CoroutineStart.LAZY) {
        delay(SPLASH_SHOW_TIME_MILLIS)
        if (storage.isJobProcessingActivated) {
            _showMainScreen
        } else {
            _showIntroScreen
        }.postValue(ShowScreen)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    fun onViewCreated() {
        showNextScreenJob.start()
    }

    fun onResume() {
        if (showNextScreenJob.isCompleted) {
            _showIntroScreen.postValue(ShowScreen)
        }
    }

    override fun onCleared() {
        showNextScreenJob.cancel()
        super.onCleared()
    }

}