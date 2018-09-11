package network.path.mobilenode.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

private val Context.foregroundServiceIntent
    get() = Intent(this, ForegroundService::class.java)

fun Context.startMinerService() {
    ContextCompat.startForegroundService(this, foregroundServiceIntent)
}

fun Context.startAndBindMinerService(serviceConnection: MinerServiceConnection) {
    startMinerService()
    bindService(foregroundServiceIntent, serviceConnection, 0)
}

fun Context.stopAndUnbindMinerService(serviceConnection: MinerServiceConnection) {
    unbindService(serviceConnection)
    stopService(foregroundServiceIntent)
}