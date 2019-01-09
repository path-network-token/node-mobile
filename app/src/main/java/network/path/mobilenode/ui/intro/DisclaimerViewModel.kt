package network.path.mobilenode.ui.intro

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.jsoup.Jsoup
import timber.log.Timber
import java.lang.ref.WeakReference

class DisclaimerViewModel : ViewModel() {
    companion object {
        private const val DISCLAIMER_URL = "https://path.net/mobile-node-disclaimer"
        private const val DIV_CLASS = "mobile-node-disclaimer-text"
    }

    private val _disclaimer = MutableLiveData<String>()
    val disclaimer: LiveData<String?> = _disclaimer

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private lateinit var loader: DataLoader

    fun onViewCreated() {
        _isLoading.postValue(true)
        loader = DataLoader(WeakReference(this))
        loader.execute()
    }

    override fun onCleared() {
        loader.cancel(true)
        super.onCleared()
    }

    private class DataLoader(private val ref: WeakReference<DisclaimerViewModel>) : AsyncTask<Void?, Void?, String?>() {
        override fun doInBackground(vararg params: Void?): String? = try {
            Jsoup.connect(DISCLAIMER_URL).get().run {
                val el = select("div.$DIV_CLASS").first()
                el.html()
            }
        } catch (e: Exception) {
            Timber.w("DISCLAIMER: failed to load online content [$e]")
            null
        }

        override fun onPostExecute(result: String?) {
            val model = ref.get()
            if (model != null) {
                model._disclaimer.postValue(result)
                model._isLoading.postValue(false)
            }
        }
    }
}
