package network.path.mobilenode.ui.intro

import androidx.lifecycle.ViewModel
import network.path.mobilenode.service.PathServiceLauncher
import network.path.mobilenode.storage.Storage

class IntroViewModel(
        private val serviceLauncher: PathServiceLauncher,
        private val storage: Storage
) : ViewModel() {

    fun onActivateClick() {
        storage.isJobProcessingActivated = true
        serviceLauncher.startService()
    }
}