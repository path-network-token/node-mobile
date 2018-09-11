package network.path.mobilenode.websocket

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.tinder.scarlet.retry.LinearBackoffStrategy
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.tinder.streamadapter.coroutines.CoroutinesStreamAdapterFactory
import kotlinx.coroutines.experimental.Job
import network.path.mobilenode.BuildConfig
import network.path.mobilenode.Constants
import network.path.mobilenode.json.PathGsonMessageAdapter
import network.path.mobilenode.service.OkHttpClientFactory

class WebSocketClient(job: Job) {
    private val lifecycleRegistry = LifecycleRegistry()
    val pathService: PathService

    init {
        job.invokeOnCompletion {
            lifecycleRegistry.onNext(Lifecycle.State.Destroyed)
        }

        val okHttpClient = OkHttpClientFactory.create()
        val webSocketFactory = okHttpClient.newWebSocketFactory(BuildConfig.WEBSOCKET_SERVER_URL)

        val gson = GsonBuilder()
            .setLenient()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

        pathService = Scarlet.Builder()
            .webSocketFactory(webSocketFactory)
            .addMessageAdapterFactory(PathGsonMessageAdapter.Factory(gson))
            .addStreamAdapterFactory(CoroutinesStreamAdapterFactory())
            .backoffStrategy(LinearBackoffStrategy(Constants.TIMEOUT_MILLIS))
            .lifecycle(lifecycleRegistry)
            .build()
            .create()
    }

    fun connect() {
        lifecycleRegistry.onNext(Lifecycle.State.Started)
    }

    fun reconnect() {
        lifecycleRegistry.onNext(Lifecycle.State.Stopped.AndAborted)
        lifecycleRegistry.onNext(Lifecycle.State.Started)
    }
}