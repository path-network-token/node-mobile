package pl.droidsonroids.minertest

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

const val LOG_TAG = "MinerTag"

class MainActivity : AppCompatActivity() {

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
    }
}
