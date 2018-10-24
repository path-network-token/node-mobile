package network.path.mobilenode.ui.intro

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import kotlinx.android.synthetic.main.fragment_disclaimer.*
import network.path.mobilenode.R
import network.path.mobilenode.ui.base.BaseFragment

class DisclaimerFragment : BaseFragment() {
    override val layoutResId = R.layout.fragment_disclaimer

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDisclaimerBody()
        setupCloseScreenButton()
    }

    private fun setupDisclaimerBody() {
        disclaimerBodyTextView.movementMethod = ScrollingMovementMethod()
    }

    private fun setupCloseScreenButton() {
        closeScreenImageView.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }
}