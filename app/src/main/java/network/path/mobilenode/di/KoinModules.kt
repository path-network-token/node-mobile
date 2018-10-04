package network.path.mobilenode.di

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.experimental.Job
import network.path.mobilenode.Constants
import network.path.mobilenode.PathNetwork
import network.path.mobilenode.runner.Runners
import network.path.mobilenode.service.LastLocationProvider
import network.path.mobilenode.service.PathServiceLauncher
import network.path.mobilenode.storage.PathRepository
import network.path.mobilenode.storage.Storage
import network.path.mobilenode.ui.intro.IntroViewModel
import network.path.mobilenode.ui.main.dashboard.DashboardViewModel
import network.path.mobilenode.ui.splash.SplashViewModel
import network.path.mobilenode.websocket.WebSocketClient
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import java.util.concurrent.TimeUnit

val appModule = module {
    single { Storage(androidApplication()) }
    single { LastLocationProvider(androidApplication()) }
    single { PathServiceLauncher(androidApplication()) }
    single { PathRepository(get()) }
    single { createOkHttpClient() }
    single { createLenientGson() }

    scope("service") { Job() }

    factory { Runners(get()) }
    factory { WebSocketClient(get(), get(), get()) }
    factory { PathNetwork(get(), get(), get(), get(), get()) }

    viewModel { IntroViewModel(get(), get()) }
    viewModel { SplashViewModel(get()) }
    viewModel { DashboardViewModel(get(), get(), get()) }
}

private fun createLenientGson(): Gson {
    return GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()
}

private fun createOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .readTimeout(Constants.JOB_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
        .writeTimeout(Constants.JOB_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
        .connectTimeout(Constants.JOB_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
        .build()
}