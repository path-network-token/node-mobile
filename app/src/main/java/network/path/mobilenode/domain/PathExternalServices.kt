package network.path.mobilenode.domain

import kotlinx.coroutines.experimental.channels.BroadcastChannel
import network.path.mobilenode.domain.entity.AutonomousSystem

interface PathExternalServices {
    val ip: BroadcastChannel<String?>
    val details: BroadcastChannel<AutonomousSystem?>

    fun start()
    fun stop()
}
