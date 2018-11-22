package network.path.mobilenode.data.http

import kotlinx.coroutines.Deferred
import retrofit2.http.GET

interface IcanHazIpService {
    @GET("/")
    fun getExternalIp(): Deferred<String>
}