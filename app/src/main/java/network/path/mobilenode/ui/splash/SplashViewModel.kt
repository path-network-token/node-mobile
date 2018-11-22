package network.path.mobilenode.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import network.path.mobilenode.domain.PathStorage
import kotlin.coroutines.CoroutineContext


class SplashViewModel(private val storage: PathStorage) : ViewModel(), CoroutineScope {
    companion object {
        private const val SPLASH_SHOW_TIME_MILLIS = 2000L
    }

    enum class NextScreen { INTRO, MAIN }

    private val _nextScreen = MutableLiveData<NextScreen>()
    val nextScreen: LiveData<NextScreen> = _nextScreen

    private val showNextScreenJob = launch(start = CoroutineStart.LAZY) {
        delay(SPLASH_SHOW_TIME_MILLIS)
//        val value = NextScreen.INTRO
        val value = if (storage.isActivated) NextScreen.MAIN else NextScreen.INTRO
        _nextScreen.postValue(value)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    fun onViewCreated() {
        showNextScreenJob.start()
    }

    fun onResume() {
        if (showNextScreenJob.isCompleted) {
            _nextScreen.postValue(NextScreen.INTRO)
        }
    }

    override fun onCleared() {
        showNextScreenJob.cancel()
        super.onCleared()
    }
}
