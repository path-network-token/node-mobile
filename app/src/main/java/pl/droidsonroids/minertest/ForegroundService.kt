package pl.droidsonroids.minertest

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
import android.widget.Toast
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

private const val WAKE_LOCK_TAG = "MinerTest::Tag"

private const val RECONNECT_DELAY_MILLIS = 10_000L
private const val REQUEST_INTERVAL_MILLIS = 30_000L

private const val NOTIFICATION_ID = 3127
private const val NOTIFICATION_NAME = "MinerTest"
private const val CHANNEL_NOTIFICATION_ID = "MinerNotificationId"

class ForegroundService : Service() {

    private val wakeLock by lazy {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG)
    }
    private val webSocketClient = WebSocketClient()
    private val compositeJob = Job()

    override fun onBind(intent: Intent) = null

    override fun onCreate() {
        super.onCreate()
        Log.d(LOG_TAG, "Foreground service onCreate")
        wakeLock.acquire()
        setUpWebSocketClient()
        startRequests()
        setUpNotificationChannelId()
        startForegroundNotification()
    }

    override fun onDestroy() {
        Log.d(LOG_TAG, "Foreground service onDestroy")
        compositeJob.cancel()
        wakeLock.release()
        webSocketClient.disconnect()
        super.onDestroy()
    }

    private fun setUpWebSocketClient() {
        webSocketClient.onMessageReceived = {
            showToast("WebSocket message: $it")
        }
        webSocketClient.onFailure = {
            showToast("WebSocket interruption")
            launch(parent = compositeJob) {
                webSocketClient.connectWithDelay(RECONNECT_DELAY_MILLIS)
            }
        }
        webSocketClient.connect()
    }

    private fun startRequests() {
        launch(parent = compositeJob) {
            webSocketClient.requestLoop(REQUEST_INTERVAL_MILLIS)
        }
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

    private fun showToast(message: String) {
        launch(UI, parent = compositeJob) {
            Toast.makeText(this@ForegroundService, message, Toast.LENGTH_SHORT).show()
        }
    }
}