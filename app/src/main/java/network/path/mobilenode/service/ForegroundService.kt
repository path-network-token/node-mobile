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
import network.path.mobilenode.BuildConfig
import network.path.mobilenode.R
import network.path.mobilenode.data.http.shadowsocks.Executable
import network.path.mobilenode.data.http.shadowsocks.GuardedProcessPool
import network.path.mobilenode.data.http.shadowsocks.Profile
import network.path.mobilenode.domain.PathSystem
import network.path.mobilenode.ui.MainActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.ext.android.bindScope
import org.koin.androidx.scope.ext.android.getOrCreateScope
import timber.log.Timber
import java.io.File

class ForegroundService : LifecycleService() {
    companion object {
        private const val WAKE_LOCK_TAG = "PathWakeLock::Tag"

        private const val NOTIFICATION_ID = 3127
        private const val CHANNEL_NOTIFICATION_ID = "PathNotificationId"

        private const val TIMEOUT = 600

        const val LOCALHOST = "127.0.0.1"
        val SS_LOCAL_PORT = if (BuildConfig.DEBUG) 1091 else 1081
        private val SIMPLE_OBFS_PORT = if (BuildConfig.DEBUG) 1092 else 1082
    }

    private val wakeLock by lazy {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG)
    }

    private val compositeJob by inject<Job>()
    private val system by inject<PathSystem>()

    private val ssLocal = GuardedProcessPool()
    private val simpleObfs = GuardedProcessPool()

    override fun onCreate() {
        super.onCreate()
        bindScope(getOrCreateScope("service"))
        Timber.v("Foreground service onCreate")

        // Native processes
        Executable.killAll()
        startNativeProcesses()

        setUpWakeLock()
        setUpNotificationChannelId()
        startForegroundNotification()
        system.start()
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
        system.stop()
        wakeLock.release()

        // Kill them all
        Executable.killAll()
        super.onDestroy()
    }

    private fun startNativeProcesses() {
        val profile = Profile()
        val libs = (this as Context).applicationInfo.nativeLibraryDir
        val obfsCmd = arrayListOf(
                File(libs, Executable.SIMPLE_OBFS).absolutePath,
                "-v",
                "-s", profile.host,
                "-p", profile.remotePort.toString(),
                "-l", SIMPLE_OBFS_PORT.toString(),
                "-t", TIMEOUT.toString(),
                "--obfs", "http"
//                "--obfs-host", BuildConfig.HTTP_SERVER_URL
        )
        simpleObfs.start(obfsCmd)

        val cmd = arrayListOf(
                File(libs, Executable.SS_LOCAL).absolutePath,
                "-u",
                "-v",
                "-s", LOCALHOST,
                "-p", SIMPLE_OBFS_PORT.toString(),
                "-k", profile.password,
                "-m", profile.method,
                "-b", LOCALHOST,
                "-l", SS_LOCAL_PORT.toString(),
                "-t", TIMEOUT.toString()
        )

        ssLocal.start(cmd)
    }
}
