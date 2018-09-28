package network.path.mobilenode.service

import android.content.Context

class PathServiceLauncher(private val context: Context) {

    fun startService() {
        context.startPathService()
    }
}