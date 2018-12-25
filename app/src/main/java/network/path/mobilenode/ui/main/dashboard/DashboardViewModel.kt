package network.path.mobilenode.ui.main.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import network.path.mobilenode.library.domain.PathSystem
import network.path.mobilenode.library.domain.entity.ConnectionStatus
import network.path.mobilenode.library.domain.entity.JobList
import java.util.*
import java.util.zip.Adler32

class DashboardViewModel(private val pathSystem: PathSystem) : ViewModel() {
    private val _nodeId = MutableLiveData<String?>()
    val nodeId: LiveData<String?> = _nodeId

    private val _status = MutableLiveData<ConnectionStatus>()
    val status: LiveData<ConnectionStatus> = _status

    private val _jobList = MutableLiveData<JobList>()
    val jobList: LiveData<JobList> = _jobList

    private val _isRunning = MutableLiveData<Boolean>()
    val isRunning: LiveData<Boolean> = _isRunning

    private val listener = object : PathSystem.BaseListener() {
        override fun onNodeId(nodeId: String?) = updateNodeId(nodeId)

        override fun onStatusChanged(status: ConnectionStatus) = updateStatus(status)

        override fun onJobListReceived(jobList: JobList?) = updateJobList(jobList)

        override fun onRunningChanged(isRunning: Boolean) = updateRunning(isRunning)
    }

    fun onViewCreated() {
        pathSystem.addListener(listener)
        updateStatus(pathSystem.status)
        updateNodeId(pathSystem.nodeId)
        updateJobList(pathSystem.jobList)
        updateRunning(pathSystem.isRunning)
    }

    fun toggle() {
        pathSystem.toggle()
    }

    override fun onCleared() {
        pathSystem.removeListener(listener)
        super.onCleared()
    }

    // Private methods
    private fun updateNodeId(nodeId: String?) = _nodeId.postValue(nodeId?.toAdler32hex())

    private fun updateStatus(status: ConnectionStatus) = _status.postValue(status)

    private fun updateJobList(jobList: JobList?) = _jobList.postValue(jobList)

    private fun updateRunning(isRunning: Boolean) = _isRunning.postValue(isRunning)

    private fun String.toAdler32hex(): String {
        val adler32 = Adler32()
        adler32.update(toByteArray())
        return "%08X".format(Locale.ROOT, adler32.value)
    }
}
