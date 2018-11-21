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
import com.instacart.library.truetime.TrueTimeRx
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.Main
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import network.path.mobilenode.BuildConfig
import network.path.mobilenode.R
import network.path.mobilenode.data.http.shadowsocks.Executable
import network.path.mobilenode.data.http.shadowsocks.GuardedProcessPool
import network.path.mobilenode.domain.PathSystem
import network.path.mobilenode.ui.MainActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.ext.android.bindScope
import org.koin.androidx.scope.ext.android.getOrCreateScope
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.math.abs

class ForegroundService : LifecycleService(), CoroutineScope {
    companion object {
        private const val TIMEOUT = 600

        const val LOCALHOST = "127.0.0.1"
        val SS_LOCAL_PORT = if (BuildConfig.DEBUG) 1091 else 1081
        private val SIMPLE_OBFS_PORT = if (BuildConfig.DEBUG) 1092 else 1082

        private const val PROXY_HOST = "afiasvoiuasd.net"
        private const val PROXY_PORT = 443
        private const val PROXY_PASSWORD = "PathNetwork"
        private const val PROXY_ENCRYPTION_METHOD = "aes-256-cfb"

        private const val TOGGLE_ACTION = "network.path.mobilenode.service.TOGGLE_ACTION"

        private const val REQUEST_CODE_TOGGLE = 1

        private const val NOTIFICATION_ID = 3127
        private const val CHANNEL_NOTIFICATION_ID = "PathNotificationId"
        private const val WAKE_LOCK_TAG = "PathWakeLock::Tag"

        private val SEED = listOf(
                intArrayOf(8, 11, 17, 4, 25, 16, 13, 19, 12, 7, 14, 47)
        )

        private fun generateDomains(): List<String> {
            val date = TrueTimeRx.now()
            val cal = Calendar.getInstance()
            cal.timeZone = TimeZone.getTimeZone("UTC")
            cal.time = date

            val seed = SEED[0]
            return (1..24).map {
                var year = cal.get(Calendar.YEAR).toLong()
                var month = cal.get(Calendar.MONTH).toLong()
                var day = cal.get(Calendar.DAY_OF_MONTH).toLong()
                var hour = it.toLong()
                val domain = StringBuffer("http://")
                for (i in 1..16) {
                    year = ((year xor seed[0] * year) shr seed[1]) xor ((year and 0xFFFFFFF0) shl seed[2])
                    month = ((month xor seed[3] * month) shr seed[4]) xor seed[5] * (month and 0xFFFFFFF8)
                    day = ((day xor (day shl seed[6])) shr seed[7]) xor ((day and 0xFFFFFFFE) shl seed[8])
                    hour = ((hour xor seed[9] * hour) shr seed[10]) xor (hour shl seed[11])
                    val char = ((abs(year xor month xor day xor hour) % 25) + 97).toChar()
                    domain.append(char)
                }
                domain.append(".net")
                domain.toString()
            }
        }
    }

    private val job = Job()

    private val wakeLock by lazy {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG)
    }

    private val compositeJob by inject<Job>()
    private val system by inject<PathSystem>()

    private val ssLocal = GuardedProcessPool()
    private val simpleObfs = GuardedProcessPool()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate() {
        super.onCreate()
        bindScope(getOrCreateScope("service"))
        Timber.v("SERVICE: onCreate()")

        // Native processes
        Executable.killAll()
        startNativeProcesses()

        setUpWakeLock()
        setUpNotificationChannelId()
        system.start()

        launch {
            system.isRunning.openSubscription().consumeEach {
                startForegroundNotification(it)
            }
        }
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
        Timber.v("SERVICE: onDestroy()")
        compositeJob.cancel()
        system.stop()
        wakeLock.release()

        // Kill them all
        Executable.killAll()
        super.onDestroy()
    }

    private fun startNativeProcesses() {
        val hosts = generateDomains()
        Timber.d("SERVICE: generated domains [${hosts.toSet()}]")

        val libs = (this as Context).applicationInfo.nativeLibraryDir
        val obfsCmd = mutableListOf(
                File(libs, Executable.SIMPLE_OBFS).absolutePath,
                "-s", PROXY_HOST,
                "-p", PROXY_PORT.toString(),
                "-l", SIMPLE_OBFS_PORT.toString(),
                "-t", TIMEOUT.toString(),
                "--obfs", "http"
        )
        if (BuildConfig.DEBUG) {
            obfsCmd.add("-v")
        }
        simpleObfs.start(obfsCmd)

        val cmd = mutableListOf(
                File(libs, Executable.SS_LOCAL).absolutePath,
                "-u",
                "-s", LOCALHOST,
                "-p", SIMPLE_OBFS_PORT.toString(),
                "-k", PROXY_PASSWORD,
                "-m", PROXY_ENCRYPTION_METHOD,
                "-b", LOCALHOST,
                "-l", SS_LOCAL_PORT.toString(),
                "-t", TIMEOUT.toString()
        )
        if (BuildConfig.DEBUG) {
            cmd.add("-v")
        }

        ssLocal.start(cmd)
    }
}
