package network.path.mobilenode.ui.intro

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.fragment_intro.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import network.path.mobilenode.R
import network.path.mobilenode.ui.base.BaseFragment
import network.path.mobilenode.utils.TranslationFractionProperty
import org.koin.androidx.viewmodel.ext.android.viewModel

@InternalCoroutinesApi
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class IntroFragment : BaseFragment() {
    override val layoutResId = R.layout.fragment_intro

    private val viewModel by viewModel<IntroViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activateButton.setOnClickListener {
            viewModel.onActivateClick()
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_introFragment_to_mainFragment)
        }

        disclaimerButton.setOnClickListener {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_introFragment_to_disclaimerFragment)
        }

        animateIn()
    }

    private fun animateIn() {
        headerDivider.translationX = -1000f
        activateButton.translationY = 1000f

        val animation = ValueAnimator.ofFloat(0f, 1f)
        animation.addUpdateListener {
            val value = it.animatedValue as Float
            pathDescriptionTextView?.alpha = value
            disclaimerButton?.alpha = value

            val scale = 0.5f + 0.5f * value
            pathDescriptionTextView?.scaleX = scale
            pathDescriptionTextView?.scaleY = scale
            disclaimerButton?.scaleX = scale
            disclaimerButton?.scaleY = scale
        }
        animation.duration = 400L

        val dividerAnimation = ObjectAnimator.ofFloat(headerDivider, TranslationFractionProperty(false), -0.5f, 0f)
        dividerAnimation.interpolator = AccelerateDecelerateInterpolator()
        dividerAnimation.duration = 250L

        val buttonAnimation = ObjectAnimator.ofFloat(activateButton, TranslationFractionProperty(true), 0.3f, 0f)
        buttonAnimation.interpolator = DecelerateInterpolator()
        buttonAnimation.startDelay = 150L
        buttonAnimation.duration = 250L

        val set = AnimatorSet()
        set.playTogether(dividerAnimation, buttonAnimation, animation)
        set.startDelay = 200L
        set.doOnEnd { activateButton?.updateDrawable() }
        set.start()
    }
}
