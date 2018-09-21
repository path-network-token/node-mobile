package network.path.mobilenode.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.Main
import kotlinx.coroutines.experimental.channels.consumeEach
import network.path.mobilenode.info.ConnectionStatus
import kotlin.coroutines.experimental.CoroutineContext

class PathServiceConnection(
    private val onStatusChange: (ConnectionStatus) -> Unit,
    private val onCompletedJobsCountChange: (Long) -> Unit
) : ServiceConnection, DefaultLifecycleObserver, CoroutineScope {
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        job.cancelChildren()
        val pathBinder = service as PathBinder
        launch {
            pathBinder.receiveJobCompleted().consumeEach(onCompletedJobsCountChange)
        }
        launch {
            pathBinder.receiveConnectionStatus().consumeEach(onStatusChange)
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        job.cancelChildren()
    }

    fun disconnect() {
        job.cancelChildren()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        job.cancel()
    }
}