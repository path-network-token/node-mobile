package network.path.mobilenode.ui.main.jobreport

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import network.path.mobilenode.library.domain.PathSystem
import network.path.mobilenode.library.domain.entity.CheckType
import network.path.mobilenode.library.domain.entity.CheckTypeStatistics

class JobReportViewModel(private val pathSystem: PathSystem) : ViewModel() {
    var firstStats: List<CheckTypeStatistics>? = null
        private set

    private val _statistics = MutableLiveData<List<CheckTypeStatistics>>()
    val statistics: LiveData<List<CheckTypeStatistics>> = _statistics

    private val _selectedType = MutableLiveData<CheckType?>()
    val selectedType: LiveData<CheckType?> = _selectedType

    private val listener = object : PathSystem.BaseListener() {
        override fun onStatisticsChanged(statistics: List<CheckTypeStatistics>) {
            postStatistics(statistics)
        }
    }

    fun onViewCreated() {
        pathSystem.addListener(listener)
        postStatistics(pathSystem.statistics)
    }

    fun select(type: CheckType?) {
        _selectedType.postValue(type)
    }

    override fun onCleared() {
        pathSystem.removeListener(listener)
        super.onCleared()
    }

    private fun postStatistics(statistics: List<CheckTypeStatistics>) {
        _statistics.postValue(statistics)
        if (firstStats == null) {
            firstStats = statistics
            _selectedType.postValue(statistics.maxBy { stat -> stat.count }?.type)
        } else {
            _selectedType.postValue(_selectedType.value)
        }
    }
}
