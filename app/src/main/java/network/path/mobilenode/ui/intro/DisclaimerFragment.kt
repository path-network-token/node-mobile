package network.path.mobilenode.ui.intro

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.fragment_disclaimer.*
import network.path.mobilenode.R
import network.path.mobilenode.ui.base.BaseFragment
import network.path.mobilenode.utils.TranslationFractionProperty
import network.path.mobilenode.utils.launchUrl
import network.path.mobilenode.utils.observe
import network.path.mobilenode.utils.setTextWithLinks
import org.koin.androidx.viewmodel.ext.android.viewModel


class DisclaimerFragment : BaseFragment() {
    override val layoutResId = R.layout.fragment_disclaimer

    private val viewModel by viewModel<DisclaimerViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()
        disclaimerBodyTextView.highlightColor = ContextCompat.getColor(context, android.R.color.transparent)
        disclaimerBodyTextView.movementMethod = LinkMovementMethod.getInstance()

        closeScreenImageView.setOnClickListener {
            requireActivity().onBackPressed()
        }

        viewModel.let {
            it.onViewCreated()
            it.isLoading.observe(this, ::isLoading)
            it.disclaimer.observe(this, ::onDisclaimer)
        }
        animateIn()
    }

    private fun isLoading(value: Boolean?) {
        if (value == true) loadingIndicator.show() else loadingIndicator.hide()
        loadingIndicator.visibility = if (value == true) View.VISIBLE else View.INVISIBLE
        disclaimerBodyTextView.visibility = if (value == false) View.VISIBLE else View.INVISIBLE
    }

    private fun onDisclaimer(text: String?) {
        val disclaimer = text ?: getString(R.string.disclaimer_body)
        disclaimerBodyTextView.setTextWithLinks(disclaimer) {
            requireContext().launchUrl(it)
        }
    }

    private fun animateIn() {
        updateAlpha(0f)
        val alphaAnimator = ValueAnimator.ofFloat(0f, 1f)
        alphaAnimator.addUpdateListener {
            val progress = it.animatedValue as Float
            updateAlpha(progress)
        }
        alphaAnimator.duration = 250L

        screenTitleTextView.translationY = 1000f
        val titleYAnimator = ObjectAnimator.ofFloat(screenTitleTextView, TranslationFractionProperty(true), 0.8f, 0f)
        val titleAlphaAnimator = ObjectAnimator.ofFloat(screenTitleTextView, "alpha", 0.5f, 1f)
        val titleScaleXAnimator = ObjectAnimator.ofFloat(screenTitleTextView, "scaleX", 0.5f, 1f)
        val titleScaleYAnimator = ObjectAnimator.ofFloat(screenTitleTextView, "scaleY", 0.5f, 1f)
        val titleSet = AnimatorSet()
        titleSet.playTogether(titleYAnimator, titleAlphaAnimator, titleScaleXAnimator, titleScaleYAnimator)
        titleSet.duration = 250L
        titleSet.interpolator = AccelerateDecelerateInterpolator()

        val set = AnimatorSet()
        set.playSequentially(titleSet, alphaAnimator)
        set.startDelay = 250L
        set.start()
    }

    private fun updateAlpha(alpha: Float) {
        loadingIndicator?.alpha = alpha
        disclaimerHeaderTextView?.alpha = alpha
        disclaimerBodyTextView?.alpha = alpha
        headerDivider?.alpha = alpha
    }
}
