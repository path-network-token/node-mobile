package pl.droidsonroids.minertest.websocket

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.tinder.scarlet.retry.LinearBackoffStrategy
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.tinder.streamadapter.coroutines.CoroutinesStreamAdapterFactory
import kotlinx.coroutines.experimental.Job
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

private const val HTTP_TIMEOUT_MILLIS: Long = 3_000
private const val WEBSOCKET_TIMEOUT_MILLIS: Long = 10_000
private const val WEBSOCKET_URL = "ws://jobs-api.path.network/ws" //TODO use staging server

class WebSocketClient(job: Job) {
    private val lifecycleRegistry = LifecycleRegistry()
    val minerService: MinerService

    init {
        job.invokeOnCompletion {
            lifecycleRegistry.onNext(Lifecycle.State.Destroyed)
        }

        val okHttpClient = OkHttpClient.Builder()
            .readTimeout(HTTP_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
            .writeTimeout(HTTP_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
            .connectTimeout(HTTP_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
            .build()

        val webSocketFactory = okHttpClient.newWebSocketFactory(WEBSOCKET_URL)

        val gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

        minerService = Scarlet.Builder()
            .webSocketFactory(webSocketFactory)
            .addMessageAdapterFactory(MinerGsonMessageAdapter.Factory(gson))
            .addStreamAdapterFactory(CoroutinesStreamAdapterFactory())
            .backoffStrategy(LinearBackoffStrategy(WEBSOCKET_TIMEOUT_MILLIS))
            .lifecycle(lifecycleRegistry)
            .build()
            .create()
    }

    fun connect() {
        lifecycleRegistry.onNext(Lifecycle.State.Started)
    }
}