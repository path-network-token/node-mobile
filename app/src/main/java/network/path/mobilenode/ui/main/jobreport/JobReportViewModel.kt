package network.path.mobilenode.ui.main.jobreport

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
import network.path.mobilenode.domain.entity.CheckType
import network.path.mobilenode.domain.entity.CheckTypeStatistics
import kotlin.coroutines.experimental.CoroutineContext

class JobReportViewModel(private val system: PathSystem) : ViewModel(), CoroutineScope {
    private lateinit var job: Job

    var firstStats: List<CheckTypeStatistics>? = null
        private set

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val _statistics = MutableLiveData<List<CheckTypeStatistics>>()
    val statistics: LiveData<List<CheckTypeStatistics>> = _statistics

    private val _selectedType = MutableLiveData<CheckType?>()
    val selectedType: LiveData<CheckType?> = _selectedType

    fun onViewCreated() {
        job = Job()
        registerHandler()
    }

    fun select(type: CheckType?) {
        launch { _selectedType.postValue(type) }
    }

    override fun onCleared() {
        job.cancel()
        super.onCleared()
    }

    private fun registerHandler() = launch {
        system.statistics.openSubscription().consumeEach {
            _statistics.postValue(it)
            if (firstStats == null) {
                firstStats = it
                _selectedType.postValue(it.maxBy { stat -> stat.count }?.type)
            } else {
                _selectedType.postValue(_selectedType.value)
            }
        }
    }
}
