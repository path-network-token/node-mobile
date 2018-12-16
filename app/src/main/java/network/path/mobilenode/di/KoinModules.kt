package network.path.mobilenode.di

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import network.path.mobilenode.BuildConfig
import network.path.mobilenode.Constants
import network.path.mobilenode.data.http.CustomDns
import network.path.mobilenode.data.http.PathHttpEngine
import network.path.mobilenode.data.runner.PathJobExecutorImpl
import network.path.mobilenode.data.runner.Runners
import network.path.mobilenode.data.storage.PathStorageImpl
import network.path.mobilenode.domain.PathEngine
import network.path.mobilenode.domain.PathJobExecutor
import network.path.mobilenode.domain.PathStorage
import network.path.mobilenode.domain.PathSystem
import network.path.mobilenode.service.LastLocationProvider
import network.path.mobilenode.service.NetworkMonitor
import network.path.mobilenode.ui.intro.DisclaimerViewModel
import network.path.mobilenode.ui.intro.IntroViewModel
import network.path.mobilenode.ui.main.dashboard.DashboardViewModel
import network.path.mobilenode.ui.main.jobreport.JobReportViewModel
import network.path.mobilenode.ui.opengl.glutils.ObjLoader
import network.path.mobilenode.ui.opengl.models.providers.ObjDataProvider
import network.path.mobilenode.ui.opengl.models.providers.SphereDataProvider
import network.path.mobilenode.ui.splash.SplashViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import java.io.IOException
import java.util.concurrent.TimeUnit

@InternalCoroutinesApi
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
val appModule = module {
    single<PathStorage> { PathStorageImpl(androidApplication()) }
    single { LastLocationProvider(androidApplication()) }
    single { NetworkMonitor(androidApplication()) }
    single { createOkHttpClient() }
    single { createLenientGson() }

    single { ObjDataProvider(ObjLoader(androidApplication(), "models/ico.obj", radius = 1f)) }
    single { SphereDataProvider(2, 1.1f) }

    single<PathEngine> { PathHttpEngine(get(), get(), get(), get(), get(), get()) }

    scope("service") { Job() }
    single { PathSystem(get(), get(), get(), get(), get()) }

    factory { Runners(get(), get(), get()) }

    factory<PathJobExecutor> { PathJobExecutorImpl(get()) }

    viewModel { IntroViewModel(get(), get()) }
    viewModel { SplashViewModel(get()) }
    viewModel { JobReportViewModel(get()) }
    viewModel { DashboardViewModel(get()) }
    viewModel { DisclaimerViewModel() }
}

private fun createLenientGson(): Gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

private fun createOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .readTimeout(Constants.JOB_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
        .writeTimeout(Constants.JOB_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
        .connectTimeout(Constants.JOB_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
        .addInterceptor { chain ->
            try {
                chain.proceed(chain.request())
            } catch (e: Throwable) {
                if (e is IOException) {
                    throw e
                } else {
                    throw IOException(e)
                }
            }
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
//            level = HttpLoggingInterceptor.Level.BODY
        })
        .dns(CustomDns())
        .build()
