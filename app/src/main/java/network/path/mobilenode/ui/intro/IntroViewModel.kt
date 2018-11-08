package network.path.mobilenode.ui.intro

import android.content.Context
import androidx.lifecycle.ViewModel
import network.path.mobilenode.domain.PathStorage
import network.path.mobilenode.service.startPathService

class IntroViewModel(
        private val context: Context,
        private val storage: PathStorage
) : ViewModel() {
    fun onActivateClick() {
        storage.isActivated = true
        context.startPathService()
    }
}
