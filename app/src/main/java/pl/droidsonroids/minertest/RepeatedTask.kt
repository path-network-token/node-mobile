package pl.droidsonroids.minertest

import android.os.Handler

class RepeatedTask(val intervalMillis: Long, val action : () -> Unit) {
    private val handler = Handler()

    private val runnable = object : Runnable {
        override fun run() {
            action()
            handler.postDelayed(this, intervalMillis)
        }
    }

    fun run() {
        handler.post(runnable)
    }

    fun stop() {
        handler.removeCallbacks(runnable)
    }
}