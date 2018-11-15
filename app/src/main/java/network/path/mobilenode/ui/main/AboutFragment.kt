package network.path.mobilenode.ui.main

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_job_report.*
import network.path.mobilenode.R
import network.path.mobilenode.ui.base.BaseFragment

class AboutFragment : BaseFragment() {
    override val layoutResId = R.layout.fragment_about

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCloseScreenButton()
    }

    private fun setupCloseScreenButton() {
        closeScreenImageView.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }
}
