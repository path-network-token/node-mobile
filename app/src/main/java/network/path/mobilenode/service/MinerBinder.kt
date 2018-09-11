package network.path.mobilenode.service

import android.os.Binder
import network.path.mobilenode.Miner

class MinerBinder(private val miner: Miner) : Binder() {
    fun receiveJobCompleted() = miner.receiveJobCompleted()
    fun receiveConnectionStatus() = miner.receiveConnectionStatus()
}