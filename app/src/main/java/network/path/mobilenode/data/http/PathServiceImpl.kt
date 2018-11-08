package network.path.mobilenode.data.http

import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory
import network.path.mobilenode.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PathServiceImpl(
        okHttpClient: OkHttpClient,
        gson: Gson
) : PathService by Retrofit.Builder()
        .baseUrl(BuildConfig.HTTP_SERVER_URL)
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(okHttpClient)
        .build()
        .create(PathService::class.java)
