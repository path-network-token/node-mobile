package network.path.mobilenode.ui.main.jobreport

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import network.path.mobilenode.library.domain.PathSystem
import network.path.mobilenode.library.domain.entity.CheckType
import network.path.mobilenode.library.domain.entity.CheckTypeStatistics
import kotlin.coroutines.CoroutineContext

@InternalCoroutinesApi
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
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
