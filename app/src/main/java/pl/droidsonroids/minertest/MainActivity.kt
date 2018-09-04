package pl.droidsonroids.minertest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

const val LOG_TAG = "MinerTag"

class MainActivity : AppCompatActivity() {

    private val storage by lazy { Storage(this) }
    private val serviceIntent by lazy {
        Intent(this, ForegroundService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpView()
    }

    private fun setUpView() {
        startServiceButton.setOnClickListener {
            ContextCompat.startForegroundService(this, serviceIntent)
        }
        stopServiceButton.setOnClickListener {
            stopService(serviceIntent)
        }
        saveButton.setOnClickListener {
            storage.pathWalletAddress = addressEditText.text.toString()
            hideKeyboard()
            Toast.makeText(this, getString(R.string.saved_toast), Toast.LENGTH_SHORT).show()
        }
        addressEditText.setText(storage.pathWalletAddress)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(findViewById<View>(android.R.id.content).windowToken, 0)
    }
}
