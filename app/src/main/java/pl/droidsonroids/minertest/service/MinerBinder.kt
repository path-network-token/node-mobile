package pl.droidsonroids.minertest.service

import android.os.Binder
import pl.droidsonroids.minertest.Miner

class MinerBinder(private val miner: Miner) : Binder() {
    fun receiveJobCompleted() = miner.receiveJobCompleted()
    fun receiveConnectionStatus() = miner.receiveConnectionStatus()
}