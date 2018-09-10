package pl.droidsonroids.minertest

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import pl.droidsonroids.minertest.info.ConnectionStatus
import pl.droidsonroids.minertest.info.ConnectionStatus.CONNECTED
import pl.droidsonroids.minertest.info.ConnectionStatus.DISCONNECTED
import pl.droidsonroids.minertest.service.MinerServiceConnection
import pl.droidsonroids.minertest.service.startAndBindMinerService
import pl.droidsonroids.minertest.service.stopAndUnbindMinerService

class MainActivity : AppCompatActivity() {

    private val serviceConnection = MinerServiceConnection(::setStatusText, ::setCompletedJobsCounterText)
    private val storage by lazy { Storage(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpView()
        handleServiceState()
    }

    override fun onDestroy() {
        if (storage.isServiceRunning) {
            unbindService(serviceConnection)
        }
        super.onDestroy()
    }

    private fun setUpView() {
        saveButton.setOnClickListener {
            storage.pathWalletAddress = addressEditText.text.toString()
            hideKeyboard()
            showToast(R.string.address_saved_toast)
        }
        startButton.setOnClickListener {
            startAndBindMinerService(serviceConnection)
            showToast(R.string.service_started_toast)
        }
        stopButton.setOnClickListener {
            if (storage.isServiceRunning) {
                stopAndUnbindMinerService(serviceConnection)
            }
            showToast(R.string.service_stopped_toast)
        }
        addressEditText.setText(storage.pathWalletAddress)
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
        if (storage.isServiceRunning) {
            startAndBindMinerService(serviceConnection)
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
