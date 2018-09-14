package network.path.mobilenode.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.experimental.Job
import network.path.mobilenode.ui.MainActivity
import network.path.mobilenode.PathNetwork
import network.path.mobilenode.R
import network.path.mobilenode.Storage
import timber.log.Timber

private const val WAKE_LOCK_TAG = "PathWakeLock::Tag"

private const val NOTIFICATION_ID = 3127
private const val CHANNEL_NOTIFICATION_ID = "PathNotificationId"

class ForegroundService : Service() {

    private val wakeLock by lazy {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG)
    }
    private val compositeJob = Job()

    private val storage by lazy { Storage(this) }
    private val pathNetwork by lazy { PathNetwork(compositeJob, storage, LastLocationProvider(this)) }

    override fun onBind(intent: Intent?) = PathBinder(pathNetwork)

    override fun onCreate() {
        super.onCreate()
        Timber.v("Foreground service onCreate")
        setUpWakeLock()
        setUpNotificationChannelId()
        startForegroundNotification()
        pathNetwork.start()
        storage.isServiceRunning = true
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
                NotificationManager.IMPORTANCE_MIN
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundNotification() {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        startForeground(
            NOTIFICATION_ID, NotificationCompat.Builder(this, CHANNEL_NOTIFICATION_ID)
                .setVibrate(longArrayOf(0L))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.notification_content))
                .build()
        )
    }

    override fun onDestroy() {
        Timber.v("Foreground service onDestroy")
        compositeJob.cancel()
        pathNetwork.finish()
        wakeLock.release()
        storage.isServiceRunning = false
        super.onDestroy()
    }
}