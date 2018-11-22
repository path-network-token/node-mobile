package network.path.mobilenode

import android.annotation.SuppressLint
import android.app.Application
import com.crashlytics.android.Crashlytics
import com.instacart.library.truetime.TrueTimeRx
import io.fabric.sdk.android.Fabric
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import network.path.mobilenode.di.appModule
import network.path.mobilenode.domain.PathStorage
import network.path.mobilenode.service.startPathService
import org.koin.android.ext.android.inject
import org.koin.android.ext.android.startKoin
import timber.log.Timber


@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class PathApplication : Application() {
    private val storage by inject<PathStorage>()

    override fun onCreate() {
        super.onCreate()
        initLogging()
        initTrueTime()
        startKoin(this, listOf(appModule))
        startJobProcessingIfActivated()
    }

    private fun initLogging() {
//        Timber.plant(Timber.DebugTree())
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Fabric.with(this, Crashlytics())
        }
    }

    private fun startJobProcessingIfActivated() {
        if (storage.isActivated) {
            startPathService()
        }
    }

    @SuppressLint("CheckResult")
    private fun initTrueTime() {
        TrueTimeRx.build()
                .initializeRx("time.google.com")
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { date -> Timber.d("TrueTime initialised: [$date]") },
                        { throwable -> Timber.w("TrueTime initialisation failed: $throwable") }
                )
    }
}
