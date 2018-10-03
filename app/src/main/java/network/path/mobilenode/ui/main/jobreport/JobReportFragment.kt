package network.path.mobilenode.ui.main.jobreport

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_job_report.*
import kotlinx.android.synthetic.main.job_types_layout.*
import network.path.mobilenode.R
import network.path.mobilenode.ui.BaseFragment

class JobReportFragment : BaseFragment() {

    override val layoutResId = R.layout.fragment_job_report

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCloseScreenButton()
        setupJobTypesClickListeners()
    }

    private fun setupCloseScreenButton() {
        closeScreenImageView.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupJobTypesClickListeners() {
        jobTypesButtonsPanel.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.httpChecksButton -> {/* TODO: PAN-8 */ }
                R.id.dnsChecksButton -> {/* TODO: PAN-8 */ }
                R.id.customChecksButton -> {/* TODO: PAN-8 */ }
            }
        }
    }
}