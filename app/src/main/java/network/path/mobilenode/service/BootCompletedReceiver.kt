package network.path.mobilenode.service

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import network.path.mobilenode.library.domain.PathSystem
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class BootCompletedReceiver : BroadcastReceiver(), KoinComponent {
    private val pathSystem by inject<PathSystem>()

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        if (pathSystem.autoStart) {
            context.startPathService()
        }
    }
}