package network.path.mobilenode.service

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import network.path.mobilenode.library.domain.PathSystem
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

@InternalCoroutinesApi
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class BootCompletedReceiver : BroadcastReceiver(), KoinComponent {
    private val system by inject<PathSystem>()

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        if (system.storage.isActivated) {
            context.startPathService()
        }
    }
}