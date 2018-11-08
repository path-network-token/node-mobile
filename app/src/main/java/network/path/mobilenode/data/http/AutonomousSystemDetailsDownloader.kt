package network.path.mobilenode.data.http

import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory
import network.path.mobilenode.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AutonomousSystemDetailsDownloader(
    okHttpClient: OkHttpClient,
    gson: Gson
) : IpToAutonomousSystemService by Retrofit.Builder()
    .baseUrl(BuildConfig.IPTOASN_BASE_URL)
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .addConverterFactory(GsonConverterFactory.create(gson))
    .client(okHttpClient)
    .build()
    .create(IpToAutonomousSystemService::class.java)