package network.path.mobilenode.http

import kotlinx.coroutines.experimental.Deferred
import network.path.mobilenode.model.AutonomousSystem
import retrofit2.http.GET
import retrofit2.http.Path

interface IpToAutonomousSystemService {
    @GET("v1/as/ip/{ip}")
    fun getAutonomousSystem(@Path("ip") ip: String): Deferred<AutonomousSystem>
}