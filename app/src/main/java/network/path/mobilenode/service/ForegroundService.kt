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
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import network.path.mobilenode.R
import network.path.mobilenode.library.domain.PathSystem
import network.path.mobilenode.ui.MainActivity
import org.koin.android.ext.android.inject
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

@InternalCoroutinesApi
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class ForegroundService : LifecycleService(), CoroutineScope {
    companion object {
        private const val TOGGLE_ACTION = "network.path.mobilenode.service.TOGGLE_ACTION"

        private const val REQUEST_CODE_TOGGLE = 1

        private const val NOTIFICATION_ID = 3127
        private const val CHANNEL_NOTIFICATION_ID = "PathNotificationId"
        private const val WAKE_LOCK_TAG = "PathWakeLock::Tag"
    }

    private val job = Job()

    private val wakeLock by lazy {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG)
    }

    private val system by inject<PathSystem>()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate() {
        super.onCreate()
        Timber.d("PATH SERVICE: onCreate()")

        setUpWakeLock()
        setUpNotificationChannelId()
        createStatusHandler()
        startForegroundNotification(false)
        system.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == TOGGLE_ACTION) {
            system.toggle()
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

    private fun createStatusHandler() = launch {
        system.isRunning.openSubscription().consumeEach {
            startForegroundNotification(it)
        }
    }

    private fun startForegroundNotification(isRunning: Boolean) {
        val action = Intent(this, ForegroundService::class.java)
        action.action = TOGGLE_ACTION
        val label = getString(if (isRunning) R.string.notification_action_pause else R.string.notification_action_resume)
        val pendingAction = PendingIntent.getService(this, REQUEST_CODE_TOGGLE, action, PendingIntent.FLAG_UPDATE_CURRENT)

        val title = getString(if (isRunning) R.string.notification_title_running else R.string.notification_title_paused)
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = NotificationCompat.Builder(this, CHANNEL_NOTIFICATION_ID)
                .setVibrate(longArrayOf(0L))
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .addAction(0, label, pendingAction)

        startForeground(NOTIFICATION_ID, builder.build())
    }

    override fun onDestroy() {
        Timber.d("PATH SERVICE: onDestroy()")
        system.stop()
        wakeLock.release()

        super.onDestroy()
    }
}

@InternalCoroutinesApi
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
private val Context.foregroundServiceIntent
    get() = Intent(this, ForegroundService::class.java)

@InternalCoroutinesApi
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
fun Context.startPathService() =
        ContextCompat.startForegroundService(this, foregroundServiceIntent)
