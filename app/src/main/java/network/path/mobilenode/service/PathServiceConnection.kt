package network.path.mobilenode.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.cancelChildren
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import network.path.mobilenode.info.ConnectionStatus

class PathServiceConnection(
    private val onStatusChange: (ConnectionStatus) -> Unit,
    private val onCompletedJobsCountChange: (Long) -> Unit
) : ServiceConnection {
    private val job = Job()

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        job.cancelChildren()
        val pathBinder = service as PathBinder
        launch(context = UI, parent = job) {
            pathBinder.receiveJobCompleted().consumeEach(onCompletedJobsCountChange)
        }
        launch(context = UI, parent = job) {
            pathBinder.receiveConnectionStatus().consumeEach(onStatusChange)
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        job.cancelChildren()
    }

    fun disconnect() {
        job.cancelChildren()
    }
}