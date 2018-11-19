package network.path.mobilenode.ui.intro

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.IO
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import org.jsoup.Jsoup
import kotlin.coroutines.experimental.CoroutineContext

class DisclaimerViewModel : ViewModel(), CoroutineScope {
    companion object {
        private const val DISCLAIMER_URL = "https://path.net/mobile-node-disclaimer"
        private const val DIV_CLASS = "mobile-node-disclaimer-text"
    }

    private val _disclaimer = MutableLiveData<String>()
    val disclaimer: LiveData<String?> = _disclaimer

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    fun onViewCreated() {
        _isLoading.postValue(true)
        loadDisclaimer()
    }

    private fun loadDisclaimer() = launch {
        try {
            Jsoup.connect(DISCLAIMER_URL).get().run {
                val el = select("div.$DIV_CLASS").first()
                _disclaimer.postValue(el.html())
                _isLoading.postValue(false)
            }
        } catch (e: Exception) {
            _isLoading.postValue(false)
            _disclaimer.postValue(null)
        }
    }

    override fun onCleared() {
        job.cancel()
        super.onCleared()
    }
}
