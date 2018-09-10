package pl.droidsonroids.minertest.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

val Context.foregroundServiceIntent
    get() = Intent(this, ForegroundService::class.java)

fun Context.startForegroundMinerService() {
    ContextCompat.startForegroundService(this, foregroundServiceIntent)
}