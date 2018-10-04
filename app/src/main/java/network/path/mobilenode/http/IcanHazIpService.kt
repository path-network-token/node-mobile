package network.path.mobilenode.http

import kotlinx.coroutines.experimental.Deferred
import retrofit2.http.GET

interface IcanHazIpService {
    @GET("/")
    fun getExternalIp(): Deferred<String>
}