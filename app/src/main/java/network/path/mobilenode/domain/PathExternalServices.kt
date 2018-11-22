package network.path.mobilenode.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import network.path.mobilenode.domain.entity.AutonomousSystem

@ExperimentalCoroutinesApi
interface PathExternalServices {
    val ip: BroadcastChannel<String?>
    val details: BroadcastChannel<AutonomousSystem?>

    fun start()
    fun stop()
}
