package network.path.mobilenode.ui.intro

import androidx.lifecycle.ViewModel
import network.path.mobilenode.Storage
import network.path.mobilenode.service.PathServiceLauncher

class IntroViewModel(
        private val serviceLauncher: PathServiceLauncher,
        private val storage: Storage
) : ViewModel() {

    fun onActivateClick() {
        storage.isJobProcessingActivated = true
        serviceLauncher.startService()
    }
}