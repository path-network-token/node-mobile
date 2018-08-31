package pl.droidsonroids.minertest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.PowerManager
import android.support.v4.app.NotificationCompat
import android.util.Log

private const val RECONNECT_DELAY_MILLIS = 10_000L
private const val REQUEST_INTERVAL_MILLIS = 3_000L
private const val MINER_NOTIFICATION_ID = "MinerNotificationId"
private const val WAKE_LOCK_TAG = "MinerTest::Tag"
private const val NOTIFICATION_NAME = "MinerTest"
private const val NOTIFICATION_ID = 3127

class ForegroundService : Service() {

    private val wakeLock by lazy {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG)
    }
    private val webSocketClient = WebSocketClient()
    private val repeatedTask = RepeatedTask(REQUEST_INTERVAL_MILLIS) { webSocketClient.send() }
    private val handler = Handler()

    override fun onBind(intent: Intent) = null

    override fun onCreate() {
        super.onCreate()
        Log.d(LOG_TAG, "Foreground service onCreate")
        wakeLock.acquire()
        setUpWebSocketClient()
        webSocketClient.connect()
        repeatedTask.run()
        setUpNotificationChannelId()
        startForegroundNotification()
    }

    private fun setUpWebSocketClient() {
        webSocketClient.onFailure = {
            handler.postDelayed({
                webSocketClient.connect()
            }, RECONNECT_DELAY_MILLIS)
        }
    }

    override fun onDestroy() {
        Log.d(LOG_TAG, "Foreground service onDestroy")
        wakeLock.release()
        repeatedTask.stop()
        webSocketClient.disconnect()
        super.onDestroy()
    }

    private fun setUpNotificationChannelId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                MINER_NOTIFICATION_ID,
                NOTIFICATION_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundNotification() {
        startForeground(
            NOTIFICATION_ID, NotificationCompat.Builder(this, MINER_NOTIFICATION_ID)
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
}