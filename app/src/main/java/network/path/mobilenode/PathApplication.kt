package network.path.mobilenode

import android.app.Application
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import network.path.mobilenode.di.appModule
import network.path.mobilenode.library.domain.PathSystem
import network.path.mobilenode.service.startPathService
import org.koin.android.ext.android.inject
import org.koin.android.ext.android.startKoin
import timber.log.Timber


class PathApplication : Application() {
    private val pathSystem by inject<PathSystem>()

    override fun onCreate() {
        super.onCreate()
        initLogging()
        startKoin(this, listOf(appModule))
        // DEBUG START
        // pathSystem.storage.isActivated = false
        // DEBUG END
        if (pathSystem.autoStart) {
            startPathService()
        }
    }

    private fun initLogging() {
//        Timber.plant(Timber.DebugTree())
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Fabric.with(this, Crashlytics())
        }
    }
}
