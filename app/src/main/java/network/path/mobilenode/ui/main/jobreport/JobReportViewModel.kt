package network.path.mobilenode.ui.main.jobreport

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import network.path.mobilenode.library.domain.PathSystem
import network.path.mobilenode.library.domain.entity.JobType
import network.path.mobilenode.library.domain.entity.JobTypeStatistics

class JobReportViewModel(private val pathSystem: PathSystem) : ViewModel() {
    var firstStats: List<JobTypeStatistics>? = null
        private set

    private val _statistics = MutableLiveData<List<JobTypeStatistics>>()
    val statistics: LiveData<List<JobTypeStatistics>> = _statistics

    private val _selectedType = MutableLiveData<JobType?>()
    val selectedType: LiveData<JobType?> = _selectedType

    private val listener = object : PathSystem.BaseListener() {
        override fun onStatisticsChanged(statistics: List<JobTypeStatistics>) {
            postStatistics(statistics)
        }
    }

    fun onViewCreated() {
        pathSystem.addListener(listener)
        postStatistics(pathSystem.statistics)
    }

    fun select(type: JobType?) {
        _selectedType.postValue(type)
    }

    override fun onCleared() {
        pathSystem.removeListener(listener)
        super.onCleared()
    }

    private fun postStatistics(statistics: List<JobTypeStatistics>) {
        _statistics.postValue(statistics)
        if (firstStats == null) {
            firstStats = statistics
            _selectedType.postValue(statistics.maxBy { stat -> stat.count }?.type)
        } else {
            _selectedType.postValue(_selectedType.value)
        }
    }
}
