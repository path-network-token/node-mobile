package network.path.mobilenode

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
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

        // Set up Crashlytics, disabled for debug builds
        val crashlyticsKit = Crashlytics.Builder()
            .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
            .build()
        // Initialize Fabric with the debug-disabled crashlytics.
        Fabric.with(this, crashlyticsKit)

        Timber.plant(PathTree())

        startKoin(this, listOf(appModule))
        // DEBUG START
        // pathSystem.storage.isActivated = false
        // DEBUG END
        if (pathSystem.autoStart) {
            startPathService()
        }
    }

    private class PathTree : Timber.DebugTree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (BuildConfig.DEBUG) {
                super.log(priority, tag, message, t)
            } else {
                Crashlytics.log(priority, tag, message)
                if (t != null) {
                    Crashlytics.logException(t)
                }
            }
        }
    }
}
