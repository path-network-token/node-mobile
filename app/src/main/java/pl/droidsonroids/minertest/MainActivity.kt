package pl.droidsonroids.minertest

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import pl.droidsonroids.minertest.info.ConnectionStatus
import pl.droidsonroids.minertest.info.ConnectionStatus.CONNECTED
import pl.droidsonroids.minertest.info.ConnectionStatus.DISCONNECTED
import pl.droidsonroids.minertest.service.MinerServiceConnection
import pl.droidsonroids.minertest.service.foregroundServiceIntent
import pl.droidsonroids.minertest.service.startForegroundMinerService

class MainActivity : AppCompatActivity() {

    private val serviceConnection = MinerServiceConnection(::setStatusText, ::setCompletedJobsCounterText)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startForegroundMinerService()
        setContentView(R.layout.activity_main)
        setUpView()
        bindService(foregroundServiceIntent, serviceConnection, 0)
    }

    override fun onDestroy() {
        unbindService(serviceConnection)
        super.onDestroy()
    }

    private fun setUpView() {
        val storage = Storage(this)
        saveButton.setOnClickListener {
            storage.pathWalletAddress = addressEditText.text.toString()
            hideKeyboard()
            Toast.makeText(this, R.string.saved_toast, Toast.LENGTH_SHORT).show()
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

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(findViewById<View>(android.R.id.content).windowToken, 0)
    }
}
