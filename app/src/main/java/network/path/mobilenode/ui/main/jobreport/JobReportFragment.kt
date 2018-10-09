package network.path.mobilenode.ui.main.jobreport

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.average_latency_layout.*
import kotlinx.android.synthetic.main.fragment_job_report.*
import kotlinx.android.synthetic.main.job_types_layout.*
import network.path.mobilenode.R
import network.path.mobilenode.ui.BaseFragment
import network.path.mobilenode.ui.observe
import org.koin.androidx.viewmodel.ext.android.viewModel

class JobReportFragment : BaseFragment() {

    override val layoutResId = R.layout.fragment_job_report

    private val jobReportViewModel by viewModel<JobReportViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCloseScreenButton()
        setupJobTypesClickListeners()

        jobReportViewModel.let {
            it.onViewCreated()
            it.httpLatencyMillis.observe(this, httpLatencyChart::setLatencyMillis)
            it.dnsLatencyMillis.observe(this, dnsLatencyChart::setLatencyMillis)
            it.customLatencyMillis.observe(this, customLatencyChart::setLatencyMillis)
        }
        //TODO set up percentage chart initial value
        //TODO add support for N/A value in latency checks if there is no data
    }

    private fun setupCloseScreenButton() {
        closeScreenImageView.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupJobTypesClickListeners() {
        jobTypesButtonsPanel.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.httpChecksButton -> setJobPercentage(jobReportViewModel.httpChecksPercentage.value)
                R.id.dnsChecksButton -> setJobPercentage(jobReportViewModel.dnsChecksPercentage.value)
                R.id.customChecksButton -> setJobPercentage(jobReportViewModel.customChecksPercentage.value)
            }
        }
    }

    private fun setJobPercentage(value: Int?) {
        val coercedValue = value ?: 0
        jobPercentageTextView.text = getString(R.string.job_percentage, coercedValue)
        jobPercentageProgressBar.progress = coercedValue
    }
}