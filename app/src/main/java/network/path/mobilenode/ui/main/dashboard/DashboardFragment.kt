package network.path.mobilenode.ui.main.dashboard

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.dashboard_details.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.job_report_button.*
import network.path.mobilenode.R
import network.path.mobilenode.ui.BaseFragment
import network.path.mobilenode.ui.observe
import org.koin.androidx.viewmodel.ext.android.viewModel

class DashboardFragment : BaseFragment() {

    override val layoutResId = R.layout.fragment_dashboard
    private val dashboardViewModel by viewModel<DashboardViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClicks()
        dashboardViewModel.let {
            it.nodeId.observe(this, ::setNodeId)
            it.isConnected.observe(this, ::colorConnectionStatusDot)
            it.operatorDetails.observe(this, ::setOperatorDetails)
        }
    }

    private fun setupClicks() {
        viewJobReportButton.setOnClickListener {
            NavHostFragment.findNavController(this)
                .navigate(R.id.action_mainFragment_to_jobReportFragment)
        }
    }

    private fun setNodeId(nodeId: String?) {
        nodeIdTextView.text = getString(
            R.string.node_id, nodeId
                ?: getString(R.string.unconfirmed_node_id)
        )
    }

    private fun colorConnectionStatusDot(isConnected: Boolean) {
        val colorRes = if (isConnected) R.color.apple_green else R.color.coral_pink
        connectionStatusDot.drawable.setTint(
            ContextCompat.getColor(requireContext(), colorRes)
        )
    }

    private fun setOperatorDetails(details: OperatorDetails) {
        operatorAsn.text = details.operatorAsn
        autonomousService.text = details.autonomousService
        country.text = details.country
    }

    companion object {
        fun newInstance() = DashboardFragment()
    }
}