package pl.droidsonroids.minertest

import android.content.Context
import android.preference.PreferenceManager
import kotlin.reflect.KProperty

private const val PATH_ADDRESS_KEY = "PATH_ADDRESS_KEY"
private const val PATH_DEFAULT_WALLET_ADDRESS = "0xF1CD6d591161A7470db74d7556876A7b5C6B9135"

private const val MINER_ID_KEY = "MINER_ID_KEY"
private const val COMPLETED_JOBS_KEY = "COMPLETED_JOBS_KEY"
private const val IS_SERVICE_RUNNING_KEY = "IS_SERVICE_RUNNING_KEY"

class Storage(context: Context) {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var pathWalletAddress by stringPref(PATH_ADDRESS_KEY, PATH_DEFAULT_WALLET_ADDRESS)
    var minerId by nullableStringPref(MINER_ID_KEY)
    var completedJobsCount by longPref(COMPLETED_JOBS_KEY)
    var isServiceRunning by booleanPref(IS_SERVICE_RUNNING_KEY)

    private fun nullableStringPref(prefKey: String, defaultValue: String? = null) =
        NullableStringStorageDelegate(prefKey, defaultValue)

    private fun stringPref(prefKey: String, defaultValue: String) =
        StringStorageDelegate(prefKey, defaultValue)

    private fun longPref(prefKey: String, defaultValue: Long = 0L) =
        LongStorageDelegate(prefKey, defaultValue)

    private fun booleanPref(prefKey: String, defaultValue: Boolean = false) =
        BooleanStorageDelegate(prefKey, defaultValue)

    inner class NullableStringStorageDelegate(private val prefKey: String, private val defaultValue: String?) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): String? =
            sharedPreferences.getString(prefKey, defaultValue)

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) =
            sharedPreferences.edit().putString(prefKey, value).apply()
    }

    inner class StringStorageDelegate(private val prefKey: String, private val defaultValue: String) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): String =
            sharedPreferences.getString(prefKey, defaultValue) ?: throw IllegalStateException("a")

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