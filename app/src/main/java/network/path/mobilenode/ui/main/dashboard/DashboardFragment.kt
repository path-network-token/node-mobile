package network.path.mobilenode.ui.main.dashboard

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.fragment_dashboard.*
import network.path.mobilenode.BaseFragment
import network.path.mobilenode.R
import network.path.mobilenode.Storage
import network.path.mobilenode.info.ConnectionStatus
import network.path.mobilenode.service.PathServiceConnection
import network.path.mobilenode.service.startAndBindPathService
import network.path.mobilenode.service.stopAndUnbindPathService
import network.path.mobilenode.showToast
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class DashboardFragment : BaseFragment() {

    override val viewModel by viewModel<DashboardViewModel>()
    override val layoutResId = R.layout.fragment_dashboard

    private val serviceConnection = PathServiceConnection(::setStatusText, ::setCompletedJobsText)
    private val storage by inject<Storage>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        handleServiceState()
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
    }

    private fun setupViews() {
        startButton.setOnClickListener {
            requireContext().startAndBindPathService(serviceConnection)
            showToast(requireContext(), R.string.service_started_toast)
            startButton.isEnabled = false
            stopButton.isEnabled = true
        }
        stopButton.setOnClickListener {
            if (storage.isServiceRunning) {
                requireContext().stopAndUnbindPathService(serviceConnection)
            }
            showToast(requireContext(), R.string.service_stopped_toast)
            startButton.isEnabled = true
            stopButton.isEnabled = false
        }
    }

    private fun setCompletedJobsText(count: Long) {
        jobsCounterTextView.text = getString(R.string.completed_jobs_label, count)
    }

    private fun setStatusText(connectionStatus: ConnectionStatus) {
        val statusResId = when (connectionStatus) {
            ConnectionStatus.CONNECTED -> R.string.state_connected
            ConnectionStatus.DISCONNECTED -> R.string.state_disconnected
        }
        statusTextView.text = getString(R.string.status_label, getString(statusResId))
    }

    private fun handleServiceState() {
        if (storage.isServiceRunning) {
            requireContext().startAndBindPathService(serviceConnection)
        } else {
            setCompletedJobsText(storage.completedJobsCount)
            setStatusText(ConnectionStatus.DISCONNECTED)
        }
    }
}