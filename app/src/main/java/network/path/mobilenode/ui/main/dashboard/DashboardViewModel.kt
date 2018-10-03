package network.path.mobilenode.ui.main.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import network.path.mobilenode.info.ConnectionStatus.CONNECTED
import network.path.mobilenode.storage.PathRepository
import java.util.*
import java.util.zip.Adler32

class DashboardViewModel(
        pathRepository: PathRepository
) : ViewModel() {

    val nodeId: LiveData<String?> = Transformations.map(pathRepository.nodeId) {
        it?.toAdler32hex()
    }

    val isConnected: LiveData<Boolean> = Transformations.map(pathRepository.connectionStatus) {
        it == CONNECTED
    }

    private val _operatorDetails = MutableLiveData<OperatorDetails>()
    val operatorDetails: LiveData<OperatorDetails> = _operatorDetails
}

private fun String.toAdler32hex(): String {
    val adler32 = Adler32()
    adler32.update(toByteArray())
    return "%08X".format(Locale.ROOT, adler32.value)
}