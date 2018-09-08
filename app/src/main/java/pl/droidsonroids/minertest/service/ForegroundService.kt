package pl.droidsonroids.minertest.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.experimental.Job
import pl.droidsonroids.minertest.Miner
import pl.droidsonroids.minertest.R
import pl.droidsonroids.minertest.Storage
import timber.log.Timber

private const val WAKE_LOCK_TAG = "MinerWakeLock"

private const val NOTIFICATION_ID = 3127
private const val CHANNEL_NOTIFICATION_ID = "MinerNotificationId"

class ForegroundService : Service() {

    class MinerBinder(private val miner: Miner) : Binder() {
        fun receiveJobCompleted() = miner.receiveJobCompleted()
        fun receiveConnectionStatus() = miner.receiveConnectionStatus()
    }

    private val wakeLock by lazy {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG)
    }
    private val compositeJob = Job()

    private val miner by lazy { Miner(compositeJob, Storage(this)) }

    override fun onBind(intent: Intent?) = MinerBinder(miner)

    override fun onCreate() {
        super.onCreate()
        Timber.v("Foreground service onCreate")
        setUpWakeLock()
        setUpNotificationChannelId()
        startForegroundNotification()
        miner.start()
    }

    @SuppressLint("WakelockTimeout") //service should work until explicitly stopped
    private fun setUpWakeLock() {
        wakeLock.acquire()
    }

    private fun setUpNotificationChannelId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_NOTIFICATION_ID,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundNotification() {
        startForeground(
            NOTIFICATION_ID, NotificationCompat.Builder(this, CHANNEL_NOTIFICATION_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .build()
        )
    }

    override fun onDestroy() {
        Timber.v("Foreground service onDestroy")
        compositeJob.cancel()
        wakeLock.release()
        super.onDestroy()
    }
}