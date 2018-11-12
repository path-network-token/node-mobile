package network.path.mobilenode.ui.main.dashboard

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.dashboard_details.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.job_report_button.*
import network.path.mobilenode.R
import network.path.mobilenode.domain.entity.AutonomousSystem
import network.path.mobilenode.domain.entity.JobList
import network.path.mobilenode.ui.base.BaseFragment
import network.path.mobilenode.utils.observe
import org.koin.androidx.viewmodel.ext.android.viewModel

class DashboardFragment : BaseFragment() {
    companion object {
        fun newInstance() = DashboardFragment()
    }

    override val layoutResId = R.layout.fragment_dashboard

    private val dashboardViewModel by viewModel<DashboardViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClicks()
        dashboardViewModel.let {
            it.onViewCreated()
            it.nodeId.observe(this, ::setNodeId)
            it.isConnected.observe(this, ::colorConnectionStatusDot)
            it.operatorDetails.observe(this, ::setOperatorDetails)
            it.ipAddress.observe(this, ::setIpAddress)
            it.jobList.observe(this, ::setJobList)
        }
    }

    private fun setupClicks() {
        viewJobReportButton.setOnClickListener {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_mainFragment_to_jobReportFragment)
        }
    }

    private fun setNodeId(nodeId: String?) {
        nodeIdTextView.text = getString(R.string.node_id, nodeId
                ?: getString(R.string.unconfirmed_node_id))
    }

    private fun setIpAddress(ipAddress: String?) {
        ipWithSubnetAddress.text = ipAddress ?: getString(R.string.n_a)
    }

    private fun colorConnectionStatusDot(isConnected: Boolean) {
        val colorRes = if (isConnected) R.color.apple_green else R.color.coral_pink
        ImageViewCompat.setImageTintList(connectionStatusDot,
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), colorRes)))

        val colorLabelRes = if (isConnected) R.color.tealish else R.color.coral_pink
        subnetAddressLabel.setBackgroundColor(ContextCompat.getColor(requireContext(), colorLabelRes))
    }

    private fun setOperatorDetails(details: AutonomousSystem?) {
        operatorAsn.text = details?.asNumber.orNoData()
        autonomousSystemDescription.text = details?.asDescription.orNoData()
        country.text = details?.asCountryCode.orNoData()
    }

    private fun setJobList(jobList: JobList) {
        operatorAsn.text = jobList.asn?.orNoData()
        ipWithSubnetAddress.text = jobList.networkPrefix ?: getString(R.string.n_a)
    }

    private fun String?.orNoData() = this ?: getString(R.string.no_data)
}