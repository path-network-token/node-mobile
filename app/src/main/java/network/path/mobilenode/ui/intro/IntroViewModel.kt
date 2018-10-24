package network.path.mobilenode.ui.intro

import android.content.Context
import androidx.lifecycle.ViewModel
import network.path.mobilenode.data.storage.Storage
import network.path.mobilenode.service.startPathService

class IntroViewModel(
        private val context: Context,
        private val storage: Storage
) : ViewModel() {

    fun onActivateClick() {
        storage.isJobProcessingActivated = true
        context.startPathService()
    }
}