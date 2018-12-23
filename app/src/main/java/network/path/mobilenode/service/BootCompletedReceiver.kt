package network.path.mobilenode.service

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import network.path.mobilenode.library.domain.PathStorage
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class BootCompletedReceiver : BroadcastReceiver(), KoinComponent {
    private val storage by inject<PathStorage>()

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        if (storage.isActivated) {
            context.startPathService()
        }
    }
}