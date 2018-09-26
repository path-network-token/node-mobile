package network.path.mobilenode.ui.intro

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.view.animation.AnimationUtils

import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_loading.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.Main
import network.path.mobilenode.BaseFragment
import network.path.mobilenode.R
import kotlin.coroutines.experimental.CoroutineContext

class LoadingFragment : BaseFragment(), CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override val layoutResId = R.layout.fragment_loading

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepareSwitchTextAnimation()
        animateLogs()
    }

    private fun animateLogs() {
        launch {
            loadingStepsTextSwitcher.setText("CHECKING ASN - COMPLETE")
            delay(500)
            loadingStepsTextSwitcher.setText("CHECKING OPERATOR ASN - COMPLETE")
            delay(500)
            loadingStepsTextSwitcher.setText("LOCATING COUNTRY ORIGIN")
        }
    }

    private fun prepareSwitchTextAnimation() {
        loadingStepsTextSwitcher.setFactory {
            TextView(ContextThemeWrapper(context, R.style.ProgressLogs), null, 0)
        }

        val inAnim = AnimationUtils.loadAnimation(context, R.anim.slide_in_down)
            .apply { duration = 300 }

        val outAnim = AnimationUtils.loadAnimation(context, R.anim.slide_out_up)
            .apply { duration = 300 }

        loadingStepsTextSwitcher.inAnimation = inAnim
        loadingStepsTextSwitcher.outAnimation = outAnim
    }
}