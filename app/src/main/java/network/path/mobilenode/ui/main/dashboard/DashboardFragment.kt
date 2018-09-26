package network.path.mobilenode.ui.main.dashboard

import android.Manifest
import android.os.Bundle
import android.view.View
import network.path.mobilenode.BaseFragment
import network.path.mobilenode.R
import network.path.mobilenode.Storage
import network.path.mobilenode.info.ConnectionStatus
import network.path.mobilenode.service.PathServiceConnection
import network.path.mobilenode.service.startAndBindPathService
import network.path.mobilenode.showToast
import org.koin.android.ext.android.inject

class DashboardFragment : BaseFragment() {

    override val layoutResId = R.layout.dash

    private val serviceConnection = PathServiceConnection(::setStatusText, ::setCompletedJobsText)
    private var isServiceBound = false

    private val storage by inject<Storage>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        lifecycle.addObserver(serviceConnection)
        setupServiceConnection()
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
    }

    private fun setupViews() {
        setCompletedJobsText(storage.completedJobsCount)
        setStatusText(ConnectionStatus.DISCONNECTED)


            isServiceBound = requireContext().startAndBindPathService(serviceConnection)
            storage.isPathNetworkEnabled = true
            showToast(requireContext(), R.string.service_started_toast)




            storage.isPathNetworkEnabled = false
            releaseServiceConnection()
            showToast(requireContext(), R.string.service_stopped_toast)


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

    private fun setupServiceConnection() {
        if (storage.isPathNetworkEnabled) {
            isServiceBound = requireContext().startAndBindPathService(serviceConnection)
        }
    }

    override fun onDestroyView() {
        releaseServiceConnection()
        super.onDestroyView()
    }

    private fun releaseServiceConnection() {
        if (isServiceBound) {
            isServiceBound = false
            serviceConnection.disconnect()
            requireContext().unbindService(serviceConnection)
        }
    }

    companion object {
        fun newInstance() = DashboardFragment()
    }
}