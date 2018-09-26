package network.path.mobilenode.ui.main.dashboard

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.dashboard_details.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.view_job_report_button.*
import network.path.mobilenode.BaseFragment
import network.path.mobilenode.R

class DashboardFragment : BaseFragment() {

    override val layoutResId = R.layout.fragment_dashboard

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClicks()
        setNodeId(10001)
    }

    private fun setupClicks() {
        viewJobReportButton.setOnClickListener {
            NavHostFragment.findNavController(this)
                .navigate(R.id.action_mainFragment_to_jobReportFragment)
        }
    }

    private fun setNodeId(nodeId: Int) {
        nodeIdTextView.text = getString(R.string.node_id, nodeId)
    }

    private fun colorConnectionStatusDot(isConnected: Boolean) {
        val colorRes = if (isConnected) R.color.apple_green else R.color.coral_pink
        connectionStatusDot.drawable.setTint(
            ContextCompat.getColor(context!!, colorRes)
        )
    }

    private fun setDashboardDetails(details: DashboardDetailsViewState) {
        operatorAsn.text = details.operatorAsn
        autonomousService.text = details.autonomousService
        country.text = details.country
        deviceType.text = details.deviceType
    }

    companion object {
        fun newInstance() = DashboardFragment()
    }
}