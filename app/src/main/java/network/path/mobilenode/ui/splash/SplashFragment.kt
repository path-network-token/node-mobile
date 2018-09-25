package network.path.mobilenode.ui.splash

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.Main
import network.path.mobilenode.BaseFragment
import network.path.mobilenode.R
import kotlin.coroutines.experimental.CoroutineContext


class SplashFragment : BaseFragment(), CoroutineScope {
    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override val layoutResId: Int = R.layout.fragment_splash


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        openMainScreenAfterDelay(delayValue = 3000)
    }

    private fun openMainScreenAfterDelay(delayValue: Long) {
        launch {
            delay(delayValue)
            NavHostFragment.findNavController(this@SplashFragment)
                .navigate(R.id.action_splashFragment_to_mainFragment)
        }
    }
}