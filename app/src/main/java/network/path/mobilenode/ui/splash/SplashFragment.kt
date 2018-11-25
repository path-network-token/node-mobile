package network.path.mobilenode.ui.splash

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.fragment_splash.*
import network.path.mobilenode.BuildConfig
import network.path.mobilenode.R
import network.path.mobilenode.ui.base.BaseFragment
import network.path.mobilenode.utils.observe
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashFragment : BaseFragment() {
    override val layoutResId: Int = R.layout.fragment_splash

    private val viewModel by viewModel<SplashViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        version.text = getString(R.string.label_version, BuildConfig.VERSION_NAME)
        viewModel.let {
            it.onViewCreated()
            it.nextScreen.observe(this, ::showScreen)
        }
        animateIn()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    private fun showScreen(nextScreen: SplashViewModel.NextScreen) {
        val actionId = when (nextScreen) {
            SplashViewModel.NextScreen.INTRO -> R.id.action_splashFragment_to_introFragment
            SplashViewModel.NextScreen.MAIN -> R.id.action_splashFragment_to_mainFragment
        }
        NavHostFragment.findNavController(this).navigate(actionId)
    }

    private fun animateIn() {
        val logoAlphaAnimator = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f)
        val logoScaleXAnimator = ObjectAnimator.ofFloat(logo, "scaleX", 0.8f, 1f)
        val logoScaleYAnimator = ObjectAnimator.ofFloat(logo, "scaleY", 0.8f, 1f)
        val logoSet = AnimatorSet()
        logoSet.duration = 750L
        logoSet.interpolator = AccelerateDecelerateInterpolator()
        logoSet.playTogether(logoAlphaAnimator, logoScaleXAnimator, logoScaleYAnimator)


        val footerLogoAlpha = ObjectAnimator.ofFloat(footerLogo, "alpha", 0f, 1f)
        val footerTextAlpha = ObjectAnimator.ofFloat(footerText, "alpha", 0f, 1f)
        val versionAlpha = ObjectAnimator.ofFloat(version, "alpha", 0f, 1f)
        val alphaSet = AnimatorSet()
        alphaSet.interpolator = AccelerateDecelerateInterpolator()
        alphaSet.duration = 1000L
        alphaSet.playTogether(footerLogoAlpha, footerTextAlpha, versionAlpha)

        val set = AnimatorSet()
        set.playTogether(logoSet, alphaSet)
        set.start()
    }
}
