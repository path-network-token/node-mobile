package network.path.mobilenode.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.launch
import org.koin.standalone.KoinComponent

@ExperimentalCoroutinesApi
class NetworkMonitor(private val context: Context) : KoinComponent {
    private val _connected = ConflatedBroadcastChannel(false)
    val connected: BroadcastChannel<Boolean> = _connected

    private val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            checkStatus()
        }
    }

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            networkCallback = NetworkCallback()
        }
    }

    fun start() {
        checkStatus()
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> connectivityManager.registerDefaultNetworkCallback(networkCallback)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                val builder = NetworkRequest.Builder()
                connectivityManager.registerNetworkCallback(builder.build(), networkCallback)
            }
            else -> context.registerReceiver(networkReceiver, intentFilter)
        }
    }

    fun stop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } else {
            context.unregisterReceiver(networkReceiver)
        }
    }

    private fun checkStatus() {
        val networkInfo = connectivityManager.activeNetworkInfo ?: return
        updateStatus(networkInfo.isConnected)
    }

    private fun updateStatus(isConnected: Boolean) {
        if (isConnected != _connected.valueOrNull) {
            GlobalScope.launch {
                _connected.send(isConnected)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    inner class NetworkCallback : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network?) {
            updateStatus(true)
        }

        override fun onLost(network: Network?) {
            updateStatus(false)
        }
    }
}
