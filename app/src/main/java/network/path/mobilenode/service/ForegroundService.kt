package network.path.mobilenode.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.experimental.Job
import network.path.mobilenode.R
import network.path.mobilenode.domain.PathSystem
import network.path.mobilenode.ui.MainActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.ext.android.bindScope
import org.koin.androidx.scope.ext.android.getOrCreateScope
import timber.log.Timber

class ForegroundService : LifecycleService() {
    companion object {
        private const val TOGGLE_ACTION = "network.path.mobilenode.service.TOGGLE_ACTION"

        private const val REQUEST_CODE_TOGGLE = 1

        private const val NOTIFICATION_ID = 3127
        private const val CHANNEL_NOTIFICATION_ID = "PathNotificationId"
        private const val WAKE_LOCK_TAG = "PathWakeLock::Tag"
    }

    private val wakeLock by lazy {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG)
    }

    private val compositeJob by inject<Job>()
    private val system by inject<PathSystem>()

    override fun onCreate() {
        super.onCreate()
        bindScope(getOrCreateScope("service"))
        Timber.v("Foreground service onCreate")
        setUpWakeLock()
        setUpNotificationChannelId()
        startForegroundNotification()
        system.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == TOGGLE_ACTION) {
            system.toggle()
            startForegroundNotification()
        }
        return super.onStartCommand(intent, flags, startId)
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
        val action = Intent(this, ForegroundService::class.java)
        action.action = TOGGLE_ACTION
        val label = getString(if (system.isRunning) R.string.notification_action_pause else R.string.notification_action_resume)
        val pendingAction = PendingIntent.getService(this, REQUEST_CODE_TOGGLE, action, PendingIntent.FLAG_UPDATE_CURRENT)

        val title = getString(if (system.isRunning) R.string.notification_content_running else R.string.notification_content_paused)
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        startForeground(NOTIFICATION_ID,
                NotificationCompat.Builder(this, CHANNEL_NOTIFICATION_ID)
                        .setVibrate(longArrayOf(0L))
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .addAction(0, label, pendingAction)
                        .build()
        )
    }

    override fun onDestroy() {
        Timber.v("Foreground service onDestroy")
        compositeJob.cancel()
        system.stop()
        wakeLock.release()
        super.onDestroy()
    }
}
