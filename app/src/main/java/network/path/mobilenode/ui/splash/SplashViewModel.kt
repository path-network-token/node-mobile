package network.path.mobilenode.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import network.path.mobilenode.library.domain.PathSystem
import java.util.*


class SplashViewModel(private val pathSystem: PathSystem) : ViewModel() {
    companion object {
        private const val SPLASH_SHOW_TIME_MILLIS = 2000L
    }

    enum class NextScreen { INTRO, MAIN }

    private val _nextScreen = MutableLiveData<NextScreen>()
    val nextScreen: LiveData<NextScreen> = _nextScreen

    private var timer: Timer? = null
    private var timerComplete = false

    fun onViewCreated() {
        val timer = Timer("splashTimer")
        timer.schedule(object : TimerTask() {
            override fun run() {
                timerComplete = true
                val value = if (pathSystem.autoStart) NextScreen.MAIN else NextScreen.INTRO
                _nextScreen.postValue(value)
            }
        }, SPLASH_SHOW_TIME_MILLIS)
        this.timer = timer
    }

    fun onResume() {
        if (timerComplete) {
            _nextScreen.postValue(NextScreen.INTRO)
        }
    }

    override fun onCleared() {
        timer?.cancel()
        super.onCleared()
    }
}
