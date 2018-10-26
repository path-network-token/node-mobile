package network.path.mobilenode.ui.main.dashboard

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
import network.path.mobilenode.domain.entity.AutonomousSystem
import network.path.mobilenode.domain.entity.ConnectionStatus.CONNECTED
import java.util.*
import java.util.zip.Adler32
import kotlin.coroutines.experimental.CoroutineContext

class DashboardViewModel(private val system: PathSystem) : ViewModel(), CoroutineScope {
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val _nodeId = MutableLiveData<String?>()
    val nodeId: LiveData<String?> = _nodeId

    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected: LiveData<Boolean> = _isConnected

    private val _operatorDetails = MutableLiveData<AutonomousSystem?>()
    val operatorDetails: LiveData<AutonomousSystem?> = _operatorDetails

    private val _ipAddress = MutableLiveData<String?>()
    val ipAddress: LiveData<String?> = _ipAddress

    fun onViewCreated() {
        registerNodeIdHandler()
        registerStatusHandler()
        registerIpHandler()
        registerDetailsHandler()
    }

    private fun registerNodeIdHandler() = launch {
        system.nodeId.openSubscription().consumeEach {
            _nodeId.postValue(it?.toAdler32hex())
        }
    }

    private fun registerStatusHandler() = launch {
        system.status.openSubscription().consumeEach {
            _isConnected.postValue(it == CONNECTED)
        }
    }

    private fun registerIpHandler() = launch {
        system.ip.openSubscription().consumeEach {
            _ipAddress.postValue(it)
        }
    }

    private fun registerDetailsHandler() = launch {
        system.details.openSubscription().consumeEach {
            _operatorDetails.postValue(it)
        }
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
