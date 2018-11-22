package network.path.mobilenode.data.http

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import network.path.mobilenode.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class ExternalIpAddressDownloader(
        okHttpClient: OkHttpClient
) : IcanHazIpService by Retrofit.Builder()
        .baseUrl(BuildConfig.ICANHAZIP_BASE_URL)
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .addConverterFactory(ScalarsConverterFactory.create())
        .client(okHttpClient)
        .build()
        .create(IcanHazIpService::class.java)
