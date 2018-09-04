package pl.droidsonroids.minertest

import android.content.Context
import android.preference.PreferenceManager

private const val PATH_ADDRESS_KEY = "PATH_ADDRESS_KEY"

class Storage(context: Context) {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var pathWalletAddress: String
        get() = sharedPreferences.getString(PATH_ADDRESS_KEY, "")!!
        set(value) = sharedPreferences.edit().putString(PATH_ADDRESS_KEY, value).apply()
}