package pl.droidsonroids.minertest

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import pl.droidsonroids.minertest.info.ConnectionStatus
import pl.droidsonroids.minertest.info.sendCompletedJobsCountBroadcast
import pl.droidsonroids.minertest.info.sendStatusBroadcast

private const val WAKE_LOCK_TAG = "MinerTest::Tag"

private const val NOTIFICATION_ID = 3127
private const val NOTIFICATION_NAME = "MinerTest"
private const val CHANNEL_NOTIFICATION_ID = "MinerNotificationId"

class ForegroundService : Service() {

    private val storage by lazy { Storage(this) }
    private val wakeLock by lazy {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG)
    }
    private val compositeJob = Job()

    private val miner by lazy { Miner(compositeJob, storage) }

    private var connectionStatus = ConnectionStatus.DISCONNECTED
        set(value) {
            field = value
            sendStatusBroadcast(value)
        }

    override fun onBind(intent: Intent?) = null

    override fun onCreate() {
        super.onCreate()
        Log.d(LOG_TAG, "Foreground service onCreate")
        setUpWakeLock()
        setUpNotificationChannelId()
        startForegroundNotification()
        miner.start()
        subscribeToCompleteJobCounterChange()
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
                NOTIFICATION_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundNotification() {
        startForeground(
            NOTIFICATION_ID, NotificationCompat.Builder(this, CHANNEL_NOTIFICATION_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(NOTIFICATION_NAME)
                .setContentIntent(createPendingIntent())
                .build()
        )
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    private fun subscribeToCompleteJobCounterChange() {
        launch(parent = compositeJob) {
            miner.jobCompleteReceiveChannel.consumeEach {
                sendCompletedJobsCountBroadcast(it)
            }
        }
    }

    override fun onDestroy() {
        Log.d(LOG_TAG, "Foreground service onDestroy")
        compositeJob.cancel()
        wakeLock.release()
        super.onDestroy()
    }
}
