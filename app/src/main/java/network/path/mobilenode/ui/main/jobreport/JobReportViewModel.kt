package network.path.mobilenode.ui.main.jobreport

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import network.path.mobilenode.runner.CheckType
import network.path.mobilenode.storage.PathRepository

class JobReportViewModel(private val pathRepository: PathRepository) : ViewModel() {
    private val _httpLatencyMillis = MutableLiveData<Int>()
    val httpLatencyMillis: LiveData<Int> = _httpLatencyMillis

    private val _dnsLatencyMillis = MutableLiveData<Int>()
    val dnsLatencyMillis: LiveData<Int> = _dnsLatencyMillis

    private val _customLatencyMillis = MutableLiveData<Int>()
    val customLatencyMillis: LiveData<Int> = _customLatencyMillis

    private val _httpCheckPercentage = MutableLiveData<Int>()
    val httpChecksPercentage: LiveData<Int> = _httpCheckPercentage

    private val _dnsCheckPercentage = MutableLiveData<Int>()
    val dnsChecksPercentage: LiveData<Int> = _dnsCheckPercentage

    private val _customCheckPercentage = MutableLiveData<Int>()
    val customChecksPercentage: LiveData<Int> = _customCheckPercentage

    fun onViewCreated() {
        val allChecksCount = CheckType.values().map { pathRepository.getCheckStatistics(it).count }.sum()
        var customCheckCount = 0L
        var customCheckAverageLatencyMillis = 0


        CheckType.values().forEach {
            val checkStatistics = pathRepository.getCheckStatistics(it)
            val averageLatencyMillis = checkStatistics.averageLatencyMillis.toInt()

            when (it) {
                CheckType.Http -> _httpLatencyMillis.postValue(averageLatencyMillis)
                CheckType.Dns -> _dnsLatencyMillis.postValue(averageLatencyMillis)
                else -> {
                    customCheckCount += checkStatistics.count
                    customCheckAverageLatencyMillis += (averageLatencyMillis * checkStatistics.count / allChecksCount.toFloat()).toInt()
                }
            }
        }

        _customLatencyMillis.postValue(customCheckAverageLatencyMillis)

        fun calculatePercentage(value: Long): Int {
            val fraction = value / allChecksCount.toFloat()
            return (fraction * 100).toInt()
        }

        fun getChecksPercentage(checkType: CheckType): Int {
            val checkCount = pathRepository.getCheckStatistics(checkType).count
            return calculatePercentage(checkCount)
        }


        if (allChecksCount > 0) {
            _httpCheckPercentage.postValue(getChecksPercentage(CheckType.Http))
            _dnsCheckPercentage.postValue(getChecksPercentage(CheckType.Dns))
            _customCheckPercentage.postValue(calculatePercentage(customCheckCount))
        }
    }

}