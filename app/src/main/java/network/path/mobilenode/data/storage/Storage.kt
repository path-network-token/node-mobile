package network.path.mobilenode.data.storage

import android.content.Context
import android.preference.PreferenceManager
import network.path.mobilenode.data.runner.CheckType
import kotlin.reflect.KProperty

private const val PATH_ADDRESS_KEY = "PATH_ADDRESS_KEY"
private const val PATH_DEFAULT_WALLET_ADDRESS = "0x0000000000000000000000000000000000000000"

private const val NODE_ID_KEY = "NODE_ID_KEY"
private const val IS_SERVICE_RUNNING_KEY = "IS_SERVICE_RUNNING_KEY"
//Reserved keys: "COMPLETED_JOBS_KEY"

private const val CHECKS_COUNT_KEY_SUFFIX = "_CHECKS_COUNT_KEY"
private const val AVERAGE_LATENCY_KEY_SUFFIX = "_AVERAGE_LATENCY_MILLIS_KEY"

class Storage(context: Context) {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var pathWalletAddress by stringPref(PATH_ADDRESS_KEY, PATH_DEFAULT_WALLET_ADDRESS)
    var nodeId by nullableStringPref(NODE_ID_KEY)
    var isJobProcessingActivated by booleanPref(IS_SERVICE_RUNNING_KEY)
    val checkStatistics = CheckStatistics()

    private fun nullableStringPref(prefKey: String, defaultValue: String? = null) =
        NullableStringStorageDelegate(prefKey, defaultValue)

    private fun stringPref(prefKey: String, defaultValue: String) =
        StringStorageDelegate(prefKey, defaultValue)

    private fun longPref(prefKey: String, defaultValue: Long = 0L) =
        LongStorageDelegate(prefKey, defaultValue)

    private fun booleanPref(prefKey: String, defaultValue: Boolean = false) =
        BooleanStorageDelegate(prefKey, defaultValue)

    inner class CheckStatistics {
        operator fun get(type: CheckType): CheckTypeStatistics {
            val count by longPref(createPrefKey(type, CHECKS_COUNT_KEY_SUFFIX))
            val averageLatency by longPref(createPrefKey(type, AVERAGE_LATENCY_KEY_SUFFIX))
            return CheckTypeStatistics(count, averageLatency)
        }

        operator fun set(type: CheckType, checkTypeStatistics: CheckTypeStatistics) {
            var count by longPref(createPrefKey(type, CHECKS_COUNT_KEY_SUFFIX))
            var averageLatency by longPref(createPrefKey(type, AVERAGE_LATENCY_KEY_SUFFIX))
            count = checkTypeStatistics.count
            averageLatency = checkTypeStatistics.averageLatencyMillis
        }
    }

    private fun createPrefKey(type: CheckType, key: String) = "$type$key"

    inner class NullableStringStorageDelegate(private val prefKey: String, private val defaultValue: String?) {

        operator fun getValue(thisRef: Any?, property: KProperty<*>): String? =
            sharedPreferences.getString(prefKey, defaultValue)

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) =
            sharedPreferences.edit().putString(prefKey, value).apply()

    }

    inner class StringStorageDelegate(private val prefKey: String, private val defaultValue: String) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): String =
            sharedPreferences.getString(prefKey, defaultValue) ?: defaultValue

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) =
            sharedPreferences.edit().putString(prefKey, value).apply()
    }

    inner class LongStorageDelegate(private val prefKey: String, private val defaultValue: Long) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Long =
            sharedPreferences.getLong(prefKey, defaultValue)

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) =
            sharedPreferences.edit().putLong(prefKey, value).apply()
    }

    inner class BooleanStorageDelegate(private val prefKey: String, private val defaultValue: Boolean) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean =
            sharedPreferences.getBoolean(prefKey, defaultValue)

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) =
            sharedPreferences.edit().putBoolean(prefKey, value).apply()
    }
}

data class CheckTypeStatistics(
    val count: Long,
    val averageLatencyMillis: Long
)