package network.path.mobilenode.ui.intro

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import network.path.mobilenode.library.domain.PathStorage
import network.path.mobilenode.service.startPathService

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class IntroViewModel(
        private val context: Context,
        private val storage: PathStorage
) : ViewModel() {
    fun onActivateClick() {
        storage.isActivated = true
        context.startPathService()
    }
}
