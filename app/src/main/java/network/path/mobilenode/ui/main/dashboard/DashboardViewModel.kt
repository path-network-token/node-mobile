package network.path.mobilenode.ui.main.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.Main
import kotlinx.coroutines.experimental.launch
import network.path.mobilenode.http.AutonomousSystemDetailsDownloader
import network.path.mobilenode.http.ExternalIpAddressDownloader
import network.path.mobilenode.model.AutonomousSystem
import network.path.mobilenode.model.ConnectionStatus.CONNECTED
import network.path.mobilenode.storage.PathRepository
import okhttp3.OkHttpClient
import java.io.IOException
import java.util.*
import java.util.zip.Adler32
import kotlin.coroutines.experimental.CoroutineContext

class DashboardViewModel(
    pathRepository: PathRepository,
    okHttpClient: OkHttpClient,
    gson: Gson
) : ViewModel(), CoroutineScope {

    private val autonomousSystemDetailsDownloader = AutonomousSystemDetailsDownloader(okHttpClient, gson)
    private val externalIpAddressDownloader = ExternalIpAddressDownloader(okHttpClient)

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    val nodeId: LiveData<String?> = Transformations.map(pathRepository.nodeId) {
        it?.toAdler32hex()
    }

    val isConnected: LiveData<Boolean> = Transformations.map(pathRepository.connectionStatus) {
        it == CONNECTED
    }

    private val _operatorDetails = MutableLiveData<AutonomousSystem?>()
    val operatorDetails: LiveData<AutonomousSystem?> = _operatorDetails

    private val _ipAddress = MutableLiveData<String?>()
    val ipAddress: LiveData<String?> = _ipAddress

    fun onViewCreated() {
        launch {
            val externalIpAddress = getExternalIpOrNull()
            _ipAddress.postValue("$externalIpAddress/32")

            val autonomousSystem = externalIpAddress?.let { getAutonomousSystemOrNull(it) }
            _operatorDetails.postValue(autonomousSystem)
        }
    }

    private suspend fun getAutonomousSystemOrNull(externalIpAddress: String) = try {
        autonomousSystemDetailsDownloader.getAutonomousSystem(externalIpAddress)
            .await()
            .run {
                if (announced) this else null
            }
    } catch (e: IOException) {
        null
    }

    private suspend fun getExternalIpOrNull() = try {
        externalIpAddressDownloader.getExternalIp()
            .await()
            .trimEnd()
    } catch (e: IOException) {
        null
    }

    override fun onCleared() {
        job.cancel()
        super.onCleared()
    }
}

private fun String.toAdler32hex(): String {
    val adler32 = Adler32()
    adler32.update(toByteArray())
    return "%08X".format(Locale.ROOT, adler32.value)
}
