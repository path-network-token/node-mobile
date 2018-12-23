package network.path.mobilenode

import android.app.Application
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import network.path.mobilenode.di.appModule
import network.path.mobilenode.library.domain.PathStorage
import network.path.mobilenode.service.startPathService
import org.koin.android.ext.android.inject
import org.koin.android.ext.android.startKoin
import timber.log.Timber


@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class PathApplication : Application() {
    private val storage by inject<PathStorage>()

    override fun onCreate() {
        super.onCreate()
        initLogging()
        startKoin(this, listOf(appModule))
        if (storage.isActivated) {
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
