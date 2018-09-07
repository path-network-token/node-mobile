package pl.droidsonroids.minertest.service

import okhttp3.OkHttpClient
import pl.droidsonroids.minertest.Constants
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