package network.path.mobilenode.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

private val Context.foregroundServiceIntent
    get() = Intent(this, ForegroundService::class.java)

fun Context.startPathService() {
    ContextCompat.startForegroundService(this, foregroundServiceIntent)
}

fun Context.startAndBindPathService(serviceConnection: PathServiceConnection) {
    startPathService()
    bindService(foregroundServiceIntent, serviceConnection, 0)
}

fun Context.stopAndUnbindPathService(serviceConnection: PathServiceConnection) {
    unbindService(serviceConnection)
    stopService(foregroundServiceIntent)
}