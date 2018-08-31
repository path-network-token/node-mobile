package pl.droidsonroids.minertest

import android.util.Log
import okhttp3.*
import java.util.concurrent.TimeUnit

class WebSocketClient {
    var onMessageReceived: ((String) -> Unit)? = null
    var onFailure: (() -> Unit)? = null

    private val client = OkHttpClient.Builder()
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    private var request = Request.Builder()
        .url("ws://jobs-api.dev.udpflood.net/ws")
        .build()

    private lateinit var webSocket: WebSocket

    fun connect() {
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(LOG_TAG, "WebSocket connection opened")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(LOG_TAG, "WebSocket message: $text")
                onMessageReceived?.invoke(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.d(LOG_TAG, "WebSocket connection interrupted")
                t.printStackTrace()
                onFailure?.invoke()
            }
        })
    }

    fun send() {
        webSocket.send(testCheckInMessage)
    }

    fun disconnect() {
        webSocket.cancel()
    }
}

private const val testCheckInMessage = """
{
  "id": "0bad7373-9315-4795-a957-c43ec9201501",
  "type": "check-in",
  "lat": "27.35",
  "lon": "128.56",
  "wallet": "0xabcdefghijklmnopqrstuvwxyz1234567890ABCD",
  "device_type": "android"
}
"""