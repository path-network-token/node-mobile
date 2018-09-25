package network.path.mobilenode.ui.intro

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.view.animation.AnimationUtils

import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_loading.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.Main
import kotlinx.coroutines.experimental.android.UI
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

        loadingStepsTextSwitcher.setFactory {
            TextView(ContextThemeWrapper(context, R.style.ProgressLogs), null, 0)
        }

        val inAnim = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
            .apply { duration = 200 }
        val outAnim = AnimationUtils.loadAnimation(context, android.R.anim.slide_out_right)
            .apply { duration = 200 }

        loadingStepsTextSwitcher.inAnimation = inAnim
        loadingStepsTextSwitcher.outAnimation = outAnim

        launch {
            loadingStepsTextSwitcher.setText("CECKING ASN - COMPLETE")
            delay(3000)
            loadingStepsTextSwitcher.setText("CECKING ASN - COMPLETE \\nCECKING OPERATOR ASN - COMPLETE")
        }
    }
}