package network.path.mobilenode.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

private val Context.foregroundServiceIntent
    get() = Intent(this, ForegroundService::class.java)

fun Context.startPathService() = ContextCompat.startForegroundService(this, foregroundServiceIntent)