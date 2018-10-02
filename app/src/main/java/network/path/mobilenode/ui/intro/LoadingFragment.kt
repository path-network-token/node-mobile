package network.path.mobilenode.ui.intro

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.fragment_loading.*
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import network.path.mobilenode.BaseFragment
import network.path.mobilenode.R

class LoadingFragment : BaseFragment() {

    override val layoutResId = R.layout.fragment_loading

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepareSwitchTextAnimation()

        launch {
            animateLogs()
            openMainScreen()
        }
    }

    private suspend fun animateLogs() {
        loadingStepsTextSwitcher.setText("CECKING ASN - COMPLETE")
        delay(500)
        loadingStepsTextSwitcher.setText("CECKING OPERATOR ASN - COMPLETE")
        delay(500)
        loadingStepsTextSwitcher.setText("LOCATING COUNTRY ORIGIN")
    }

    private fun openMainScreen() {
        NavHostFragment.findNavController(this)
            .navigate(R.id.action_loadingFragment_to_mainFragment)
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