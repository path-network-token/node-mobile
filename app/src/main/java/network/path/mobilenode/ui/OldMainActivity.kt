package network.path.mobilenode.ui

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.EditorInfo.IME_NULL
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_old_main.*
import network.path.mobilenode.R
import network.path.mobilenode.Storage
import network.path.mobilenode.info.ConnectionStatus
import network.path.mobilenode.info.ConnectionStatus.CONNECTED
import network.path.mobilenode.info.ConnectionStatus.DISCONNECTED
import network.path.mobilenode.service.PathServiceConnection
import network.path.mobilenode.service.startAndBindPathService
import network.path.mobilenode.service.stopAndUnbindPathService

//TODO keep it until the logic is enclosed in a new UI screen version
class OldMainActivity : AppCompatActivity() {

    private val serviceConnection = PathServiceConnection(::setStatusText, ::setCompletedJobsCounterText)
    private val storage by lazy { Storage(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_old_main)
        setUpView()
        handleServiceState()
        ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION), 1)
    }

    override fun onDestroy() {
        if (storage.isPathNetworkEnabled) {
            unbindService(serviceConnection)
        }
        super.onDestroy()
    }

    private fun setUpView() {
        saveButton.setOnClickListener {
            onWalletAddressConfirmed()
        }
        activateButton.setOnClickListener {
            startAndBindPathService(serviceConnection)
            showToast(R.string.service_started_toast)
        }
        stopButton.setOnClickListener {
            if (storage.isPathNetworkEnabled) {
                stopAndUnbindPathService(serviceConnection)
            }
            showToast(R.string.service_stopped_toast)
        }
        addressEditText.setText(storage.pathWalletAddress)
        addressEditText.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                IME_NULL, IME_ACTION_DONE -> {
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

    private fun setCompletedJobsCounterText(count: Long) {
        jobsCounterTextView.text = getString(R.string.completed_jobs_label, count)
    }

    private fun setStatusText(connectionStatus: ConnectionStatus) {
        val statusResId = when (connectionStatus) {
            CONNECTED -> R.string.state_connected
            DISCONNECTED -> R.string.state_disconnected
        }
        statusTextView.text = getString(R.string.status_label, getString(statusResId))
    }

    private fun handleServiceState() {
        if (storage.isPathNetworkEnabled) {
            startAndBindPathService(serviceConnection)
        } else {
            setCompletedJobsCounterText(storage.completedJobsCount)
            setStatusText(DISCONNECTED)
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(findViewById<View>(android.R.id.content).windowToken, 0)
    }

    private fun showToast(@StringRes messageResId: Int) {
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show()
    }
}