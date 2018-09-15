package network.path.mobilenode.di

import kotlinx.coroutines.experimental.Job
import network.path.mobilenode.PathNetwork
import network.path.mobilenode.Storage
import network.path.mobilenode.runner.Runners
import network.path.mobilenode.service.LastLocationProvider
import network.path.mobilenode.service.OkHttpClientFactory
import network.path.mobilenode.websocket.WebSocketClient
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.module

val appModule = module {
    single { Storage(androidApplication()) }
    single { LastLocationProvider(androidApplication()) }

    scope("service") { Job() }
    factory { OkHttpClientFactory.create() }
    factory { Runners(get()) }
    factory {
        WebSocketClient(
            job = get(),
            okHttpClient = get()
        )
    }
    factory {
        PathNetwork(
            job = get(),
            lastLocationProvider = get(),
            storage = get(),
            webSocketClient = get(),
            runners = get()
        )
    }
}