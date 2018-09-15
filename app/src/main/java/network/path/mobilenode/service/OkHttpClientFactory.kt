package network.path.mobilenode.service

import network.path.mobilenode.Constants
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object OkHttpClientFactory {

    fun create(): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(Constants.TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
            .writeTimeout(Constants.TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
            .connectTimeout(Constants.TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
            .build()
    }
}