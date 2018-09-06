package pl.droidsonroids.minertest.info

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

private const val BROADCAST_ACTION = "network.path.miner.broadcast"
private const val EXTRA_JOB_COMPLETED_COUNT = "EXTRA_JOB_COMPLETED_COUNT"
private const val EXTRA_STATUS = "EXTRA_STATUS"

class InfoBroadcastReceiver(
    private val onCompletedJobsChanged: (Long) -> Unit,
    private val onStatusChanged: (ConnectionStatus) -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        intent.getLongExtra(EXTRA_JOB_COMPLETED_COUNT, -1L).let {
            if(it != -1L) onCompletedJobsChanged(it)
        }
        intent.getSerializableExtra(EXTRA_STATUS)?.let {
            onStatusChanged(it as ConnectionStatus)
        }
    }
}

fun Context.registerInfoReceiver(infoBroadcastReceiver: InfoBroadcastReceiver) {
    registerReceiver(infoBroadcastReceiver, IntentFilter(BROADCAST_ACTION))
}

fun Context.sendCompletedJobsCountBroadcast(count: Long) {
    Intent().apply {
        action = BROADCAST_ACTION
        putExtra(EXTRA_JOB_COMPLETED_COUNT, count)
        sendBroadcast(this)
    }
}

fun Context.sendStatusBroadcast(connectionStatus: ConnectionStatus) {
    Intent().apply {
        action = BROADCAST_ACTION
        putExtra(EXTRA_STATUS, connectionStatus)
        sendBroadcast(this)
    }
}