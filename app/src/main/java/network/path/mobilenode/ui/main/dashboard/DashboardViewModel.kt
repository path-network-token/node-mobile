package network.path.mobilenode.ui.main.dashboard

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
import network.path.mobilenode.domain.PathSystem
import network.path.mobilenode.domain.entity.ConnectionStatus
import network.path.mobilenode.domain.entity.JobList
import java.util.*
import java.util.zip.Adler32
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class DashboardViewModel(private val system: PathSystem) : ViewModel(), CoroutineScope {
    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val _nodeId = MutableLiveData<String?>()
    val nodeId: LiveData<String?> = _nodeId

    private val _status = MutableLiveData<ConnectionStatus>()
    val status: LiveData<ConnectionStatus> = _status

    private val _jobList = MutableLiveData<JobList>()
    val jobList: LiveData<JobList> = _jobList

    private val _isRunning = MutableLiveData<Boolean>()
    val isRunning: LiveData<Boolean> = _isRunning

    fun onViewCreated() {
        job = Job()
        registerNodeIdHandler()
        registerStatusHandler()
        registerJobListHandler()
        registerRunningHandler()
    }

    fun toggle() {
        system.toggle()
    }

    private fun registerNodeIdHandler() = launch {
        system.nodeId.openSubscription().consumeEach {
            _nodeId.postValue(it?.toAdler32hex())
        }
    }

    private fun registerStatusHandler() = launch {
        system.status.openSubscription().consumeEach {
            _status.postValue(it)
        }
    }

    private fun registerJobListHandler() = launch {
        system.jobList.openSubscription().consumeEach {
            _jobList.postValue(it)
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

    private fun String.toAdler32hex(): String {
        val adler32 = Adler32()
        adler32.update(toByteArray())
        return "%08X".format(Locale.ROOT, adler32.value)
    }
}
