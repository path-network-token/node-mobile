package network.path.mobilenode.ui.intro

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import network.path.mobilenode.library.domain.PathSystem
import network.path.mobilenode.service.startPathService

@InternalCoroutinesApi
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class IntroViewModel(
        private val context: Context,
        private val system: PathSystem
) : ViewModel() {
    fun onActivateClick() {
        system.storage.isActivated = true
        context.startPathService()
    }
}
