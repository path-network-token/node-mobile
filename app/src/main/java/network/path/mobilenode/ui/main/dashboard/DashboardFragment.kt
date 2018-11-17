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
import network.path.mobilenode.domain.entity.ConnectionStatus
import network.path.mobilenode.domain.entity.JobList
import network.path.mobilenode.ui.base.BaseFragment
import network.path.mobilenode.ui.opengl.OpenGLSurfaceView
import network.path.mobilenode.utils.observe
import org.koin.androidx.viewmodel.ext.android.viewModel

class DashboardFragment : BaseFragment() {
    companion object {
        private const val STATE_OPENGL = "STATE_OPENGL"

        fun newInstance() = DashboardFragment()
    }

    override val layoutResId = R.layout.fragment_dashboard

    private val dashboardViewModel by viewModel<DashboardViewModel>()

    private lateinit var openGlSurfaceView: OpenGLSurfaceView
    private var openGlState: Bundle? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dashboardViewModel.let {
            it.onViewCreated()
            it.nodeId.observe(this, ::setNodeId)
            it.status.observe(this, ::setStatus)
            it.operatorDetails.observe(this, ::setOperatorDetails)
            it.ipAddress.observe(this, ::setIpAddress)
            it.jobList.observe(this, ::setJobList)
            it.isRunning.observe(this, ::setRunning)
        }

        openGlSurfaceView = surfaceView
        restoreGlState(savedInstanceState)

        setupClicks()
    }

    override fun onPause() {
        super.onPause()
        openGlSurfaceView.onPause()
    }

    override fun onResume() {
        super.onResume()
        openGlSurfaceView.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        saveGlState()
        openGlSurfaceView.destroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveGlState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        restoreGlState(savedInstanceState)
    }

    private fun saveGlState(outState: Bundle? = null) {
        openGlState = openGlSurfaceView.saveState()
        outState?.putBundle(STATE_OPENGL, openGlState)
    }

    private fun restoreGlState(savedInstanceState: Bundle? = null) {
        val state = savedInstanceState?.getBundle(STATE_OPENGL) ?: openGlState
        if (state != null) {
            openGlSurfaceView.restoreState(state)
        }
    }

    private fun setupClicks() {
        viewJobReportButton.setOnClickListener {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_mainFragment_to_jobReportFragment)
        }

        toggleButton.setOnClickListener {
            dashboardViewModel.toggle()
        }

        infoButton.setOnClickListener {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_mainFragment_to_aboutFragment)
        }
    }

    private fun setNodeId(nodeId: String?) {
        nodeIdTextView.text = getString(R.string.node_id, nodeId
                ?: getString(R.string.unconfirmed_node_id))
    }

    private fun setIpAddress(ipAddress: String?) {
        ipWithSubnetAddress.text = ipAddress ?: getString(R.string.n_a)
    }

    private fun setStatus(status: ConnectionStatus) {
        ImageViewCompat.setImageTintList(statusDot, ColorStateList.valueOf(status.dotColor))

        subnetAddressLabel.setTextColor(status.textColor)
        labelStatus.text = status.label
        separator.setBackgroundColor(status.separatorColor)

        openGlSurfaceView.setConnectionStatus(status)
    }

    private fun setOperatorDetails(details: AutonomousSystem?) {
        value1.text = details?.asNumber.orNoData()
        value2.text = details?.asDescription.orNoData()
        value3.text = details?.asCountryCode.orNoData()
    }

    private fun setJobList(jobList: JobList) {
        value1.text = jobList.asn?.orNoData()
        ipWithSubnetAddress.text = jobList.networkPrefix ?: getString(R.string.n_a)
    }

    private fun setRunning(isRunning: Boolean) {
        toggleButton.isSelected = !isRunning
        diagonalLine.visibility = if (isRunning) View.GONE else View.VISIBLE
        labelPaused.visibility = if (isRunning) View.GONE else View.VISIBLE
        pausedBackground.visibility = if (isRunning) View.GONE else View.VISIBLE
        openGlSurfaceView.setRunning(isRunning)
    }

    private fun String?.orNoData() = this ?: getString(R.string.no_data)

    private val ConnectionStatus.label: String
        get() = getString(when (this) {
            ConnectionStatus.CONNECTED -> R.string.status_connected
            ConnectionStatus.DISCONNECTED -> R.string.status_disconnected
        })

    private val ConnectionStatus.dotColor: Int
        get() = ContextCompat.getColor(requireContext(), when (this) {
            ConnectionStatus.CONNECTED -> R.color.apple_green
            ConnectionStatus.DISCONNECTED -> R.color.coral_pink
        })

    private val ConnectionStatus.textColor: Int
        get() = ContextCompat.getColor(requireContext(), when (this) {
            ConnectionStatus.CONNECTED -> R.color.light_teal
            ConnectionStatus.DISCONNECTED -> R.color.coral_pink
        })

    private val ConnectionStatus.separatorColor: Int
        get() = ContextCompat.getColor(requireContext(), when (this) {
            ConnectionStatus.CONNECTED -> R.color.tealish
            ConnectionStatus.DISCONNECTED -> R.color.coral_pink
        })
}
