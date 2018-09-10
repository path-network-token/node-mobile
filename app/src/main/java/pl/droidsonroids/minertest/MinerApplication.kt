package pl.droidsonroids.minertest

import android.app.Application

import timber.log.Timber

class MinerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}