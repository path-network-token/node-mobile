package network.path.mobilenode.di

import kotlinx.coroutines.experimental.Job
import network.path.mobilenode.PathNetwork
import network.path.mobilenode.http.OkHttpClientFactory
import network.path.mobilenode.runner.Runners
import network.path.mobilenode.service.LastLocationProvider
import network.path.mobilenode.service.PathServiceLauncher
import network.path.mobilenode.storage.PathRepository
import network.path.mobilenode.storage.Storage
import network.path.mobilenode.ui.intro.IntroViewModel
import network.path.mobilenode.ui.main.dashboard.DashboardViewModel
import network.path.mobilenode.ui.splash.SplashViewModel
import network.path.mobilenode.websocket.WebSocketClient
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

val appModule = module {
    single { Storage(androidApplication()) }
    single { LastLocationProvider(androidApplication()) }
    single { PathServiceLauncher(androidApplication()) }
    single { PathRepository(get()) }

    scope("service") { Job() }

    factory { OkHttpClientFactory.create() }
    factory { Runners(get()) }
    factory { WebSocketClient(get(), get()) }
    factory { PathNetwork(get(), get(), get(), get(), get()) }

    viewModel { IntroViewModel(get(), get()) }
    viewModel { SplashViewModel(get()) }
    viewModel { DashboardViewModel(get()) }
}