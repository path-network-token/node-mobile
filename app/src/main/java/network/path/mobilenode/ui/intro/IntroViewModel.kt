package network.path.mobilenode.ui.intro

import android.content.Context
import androidx.lifecycle.ViewModel
import network.path.mobilenode.domain.PathSystem
import network.path.mobilenode.service.startPathService

class IntroViewModel(
        private val context: Context,
        private val system: PathSystem
) : ViewModel() {
    fun onActivateClick() {
        system.activate()
        context.startPathService()
    }
}
