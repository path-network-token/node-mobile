package network.path.mobilenode.data.http

import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.GET

interface IcanHazIpService {
    @GET("/")
    fun getExternalIp(): Deferred<String>
}