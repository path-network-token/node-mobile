package network.path.mobilenode.service

import android.os.Binder
import network.path.mobilenode.PathNetwork

class PathBinder(private val pathNetwork: PathNetwork) : Binder() {
    fun receiveJobCompleted() = pathNetwork.receiveJobCompleted()
    fun receiveConnectionStatus() = pathNetwork.receiveConnectionStatus()
}