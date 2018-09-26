package network.path.mobilenode.ui.splash

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import network.path.mobilenode.BaseFragment
import network.path.mobilenode.R


class SplashFragment : BaseFragment(), CoroutineScope {

    override val layoutResId: Int = R.layout.fragment_splash

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        openMainScreenAfterDelay(delayMillis = 3000)
    }

    private fun openMainScreenAfterDelay(delayMillis: Long) {
        launch {
            delay(delayMillis)
            NavHostFragment.findNavController(this@SplashFragment)
                .navigate(R.id.action_splashFragment_to_introFragment)
        }
    }
}