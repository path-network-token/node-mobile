package pl.droidsonroids.minertest.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

fun Context.startForegroundMinerService() {
    val intent = Intent(this, ForegroundService::class.java)
    ContextCompat.startForegroundService(this, intent)
}