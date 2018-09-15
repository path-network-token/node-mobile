package network.path.mobilenode

import android.app.Application
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import network.path.mobilenode.di.appModule
import org.koin.android.ext.android.startKoin
import timber.log.Timber

class PathApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Fabric.with(this, Crashlytics())
        }
        startKoin(this, listOf(appModule))
    }
}