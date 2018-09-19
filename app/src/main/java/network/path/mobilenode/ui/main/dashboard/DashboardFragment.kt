package network.path.mobilenode.ui.main.dashboard

import android.Manifest
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_old_main.*
import network.path.mobilenode.BaseFragment
import network.path.mobilenode.R
import network.path.mobilenode.Storage
import network.path.mobilenode.info.ConnectionStatus
import network.path.mobilenode.service.PathServiceConnection
import network.path.mobilenode.service.startAndBindPathService
import network.path.mobilenode.service.stopAndUnbindPathService
import org.koin.androidx.viewmodel.ext.android.viewModel

class DashboardFragment : BaseFragment() {

    override val viewModel by viewModel<DashboardViewModel>()
    override val layoutResId = R.layout.fragment_dashboard

    private val serviceConnection = PathServiceConnection(::setStatusText, ::setCompletedJobsText)
    private val storage by lazy { Storage(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        handleServiceState()
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
    }

    private fun setupViews() {
        startButton.setOnClickListener {
            requireContext().startAndBindPathService(serviceConnection)
            showToast(R.string.service_started_toast)
        }
        stopButton.setOnClickListener {
            if (storage.isServiceRunning) {
                requireContext().stopAndUnbindPathService(serviceConnection)
            }
            showToast(R.string.service_stopped_toast)
        }
        addressEditText.setText(storage.pathWalletAddress)
        addressEditText.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_NULL, EditorInfo.IME_ACTION_DONE -> {
                    onWalletAddressConfirmed()
                    true
                }
                else -> false
            }
        }
    }

    private fun onWalletAddressConfirmed() {
        if (addressEditText.text.isBlank()) {
            addressEditText.error = getString(R.string.blank_path_wallet_address_error)
        } else {
            updatePathWalletAddress()
            addressEditText.error = null
        }
    }

    private fun updatePathWalletAddress() {
        storage.pathWalletAddress = addressEditText.text.toString()
        hideKeyboard()
        showToast(R.string.address_saved_toast)
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

    private fun hideKeyboard() {
        val inputMethodManager = requireContext()
            .getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(
            requireActivity().findViewById<View>(android.R.id.content).windowToken,
            0
        )
    }

    private fun showToast(@StringRes messageResId: Int) {
        Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show()
    }
}