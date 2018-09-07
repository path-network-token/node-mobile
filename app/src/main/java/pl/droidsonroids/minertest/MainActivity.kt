package pl.droidsonroids.minertest

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import pl.droidsonroids.minertest.info.ConnectionStatus
import pl.droidsonroids.minertest.info.InfoBroadcastReceiver
import pl.droidsonroids.minertest.info.registerInfoReceiver
import pl.droidsonroids.minertest.service.startForegroundMinerService

class MainActivity : AppCompatActivity() {

    private val storage by lazy { Storage(this) }

    private val infoBroadcastReceiver = InfoBroadcastReceiver(
        onCompletedJobsChanged = ::setCompletedJobsCounterText,
        onStatusChanged = ::setStatusText
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startForegroundMinerService()
        setContentView(R.layout.activity_main)
        setUpView()
    }

    override fun onResume() {
        super.onResume()
        setCompletedJobsCounterText(storage.completedJobsCount)
        registerInfoReceiver(infoBroadcastReceiver)
    }

    override fun onPause() {
        unregisterReceiver(infoBroadcastReceiver)
        super.onPause()
    }

    private fun setUpView() {
        saveButton.setOnClickListener {
            storage.pathWalletAddress = addressEditText.text.toString()
            hideKeyboard()
            Toast.makeText(this, R.string.saved_toast, Toast.LENGTH_SHORT).show()
        }
        addressEditText.setText(storage.pathWalletAddress)
        setStatusText(ConnectionStatus.DISCONNECTED)
    }

    private fun setCompletedJobsCounterText(count: Long) {
        jobsCounterTextView.text = getString(R.string.completed_jobs_label, count)
    }

    private fun setStatusText(connectionStatus: ConnectionStatus) {
        val statusResId = when (connectionStatus) {
            ConnectionStatus.CONNECTED -> R.string.state_connected
            ConnectionStatus.DISCONNECTED -> R.string.state_disconnected
        }
        statusTextView.text = getString(R.string.status_label, getString(statusResId))
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(findViewById<View>(android.R.id.content).windowToken, 0)
    }
}
