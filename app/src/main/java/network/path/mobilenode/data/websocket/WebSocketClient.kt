package network.path.mobilenode.data.websocket

import com.google.gson.Gson
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.tinder.scarlet.retry.LinearBackoffStrategy
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.tinder.streamadapter.coroutines.CoroutinesStreamAdapterFactory
import kotlinx.coroutines.experimental.Job
import network.path.mobilenode.BuildConfig
import network.path.mobilenode.Constants
import network.path.mobilenode.data.json.PathGsonMessageAdapter
import okhttp3.OkHttpClient

class WebSocketClient(
    job: Job,
    okHttpClient: OkHttpClient,
    gson: Gson
) {
    private val lifecycleRegistry = LifecycleRegistry()
    val pathService: PathService

    init {
        job.invokeOnCompletion {
            lifecycleRegistry.onNext(Lifecycle.State.Destroyed)
        }

        val webSocketFactory = okHttpClient.newWebSocketFactory(BuildConfig.WEBSOCKET_SERVER_URL)

        pathService = Scarlet.Builder()
            .webSocketFactory(webSocketFactory)
            .addMessageAdapterFactory(PathGsonMessageAdapter.Factory(gson))
            .addStreamAdapterFactory(CoroutinesStreamAdapterFactory())
            .backoffStrategy(LinearBackoffStrategy(Constants.JOB_TIMEOUT_MILLIS))
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