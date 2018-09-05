package pl.droidsonroids.minertest

import android.content.Context
import android.preference.PreferenceManager
import kotlin.reflect.KProperty

private const val PATH_ADDRESS_KEY = "PATH_ADDRESS_KEY"
private const val PATH_DEFAULT_WALLET_ADDRESS = "0xF1CD6d591161A7470db74d7556876A7b5C6B9135"
private const val MINER_ID_KEY = "MINER_ID_KEY"

class Storage(context: Context) {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var pathWalletAddress by stringPref(PATH_ADDRESS_KEY, PATH_DEFAULT_WALLET_ADDRESS)
    var minerId by stringPref(MINER_ID_KEY)

    private fun stringPref(prefKey: String, defaultValue: String? = null) =
        StringStorageDelegate(prefKey, defaultValue)

    inner class StringStorageDelegate(private val prefKey: String, private val defaultValue: String?) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): String? =
            sharedPreferences.getString(prefKey, defaultValue)

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) =
            sharedPreferences.edit().putString(prefKey, value).apply()
    }
}