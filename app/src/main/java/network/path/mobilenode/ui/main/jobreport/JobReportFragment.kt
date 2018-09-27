package network.path.mobilenode.ui.main.jobreport

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_job_report.*
import kotlinx.android.synthetic.main.job_types_layout.*
import network.path.mobilenode.BaseFragment
import network.path.mobilenode.R

class JobReportFragment : BaseFragment() {

    override val layoutResId = R.layout.fragment_job_report

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCloseScreenButton()
        setupJobTypesClickListeners()
    }

    private fun setupCloseScreenButton() {
        closeScreenImageView.setOnClickListener {
            activity!!.onBackPressed()
        }
    }

    private fun setupJobTypesClickListeners() {
        httpChecksButton.setOnClickListener {
            // TODO: PAN-8
        }

        dnsChecksButton.setOnClickListener {
            // TODO: PAN-8
        }

        customChecksButton.setOnClickListener {
            // TODO: PAN-8
        }
    }
}