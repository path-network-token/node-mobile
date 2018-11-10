package network.path.mobilenode.ui.intro

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.fragment_disclaimer.*
import network.path.mobilenode.R
import network.path.mobilenode.ui.base.BaseFragment
import network.path.mobilenode.utils.launchUrl
import network.path.mobilenode.utils.setTextWithLinks


class DisclaimerFragment : BaseFragment() {
    override val layoutResId = R.layout.fragment_disclaimer

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDisclaimerBody()
        setupCloseScreenButton()
    }

    private fun setupDisclaimerBody() {
        val context = context ?: return
        disclaimerBodyTextView.highlightColor = ContextCompat.getColor(context, android.R.color.transparent)
        disclaimerBodyTextView.setTextWithLinks(getString(R.string.disclaimer_body)) {
            context.launchUrl(it)
        }
        disclaimerBodyTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setupCloseScreenButton() {
        closeScreenImageView.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }
}
