package network.path.mobilenode.ui.intro

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.fragment_disclaimer.*
import network.path.mobilenode.R
import network.path.mobilenode.ui.base.BaseFragment
import network.path.mobilenode.utils.launchUrl
import network.path.mobilenode.utils.observe
import network.path.mobilenode.utils.setTextWithLinks
import org.koin.androidx.viewmodel.ext.android.viewModel


class DisclaimerFragment : BaseFragment() {
    override val layoutResId = R.layout.fragment_disclaimer

    private val viewModel by viewModel<DisclaimerViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDisclaimerBody()
        setupCloseScreenButton()

        viewModel.let {
            it.onViewCreated()
            it.isLoading.observe(this, ::isLoading)
            it.disclaimer.observe(this, ::onDisclaimer)
        }
    }

    private fun setupDisclaimerBody() {
        val context = requireContext()
        disclaimerBodyTextView.highlightColor = ContextCompat.getColor(context, android.R.color.transparent)
        disclaimerBodyTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setupCloseScreenButton() {
        closeScreenImageView.setOnClickListener {
            requireActivity().onBackPressed()
        }
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
}
