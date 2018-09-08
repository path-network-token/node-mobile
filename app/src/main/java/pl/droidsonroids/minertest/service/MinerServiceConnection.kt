package pl.droidsonroids.minertest.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import pl.droidsonroids.minertest.info.ConnectionStatus

class MinerServiceConnection(
    private val onStatusChange: (ConnectionStatus) -> Unit,
    private val onCompletedJobsCountChange: (Long) -> Unit
) : ServiceConnection {
    private val job = Job()

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        val minerBinder = service as MinerBinder
        launch(context = UI, parent = job) {
            minerBinder.receiveJobCompleted().consumeEach(onCompletedJobsCountChange)
        }
        launch(context = UI, parent = job) {
            minerBinder.receiveConnectionStatus().consumeEach(onStatusChange)
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        job.cancel()
    }
}