package network.path.mobilenode.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
private val Context.foregroundServiceIntent
    get() = Intent(this, ForegroundService::class.java)

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
fun Context.startPathService() = ContextCompat.startForegroundService(this, foregroundServiceIntent)
