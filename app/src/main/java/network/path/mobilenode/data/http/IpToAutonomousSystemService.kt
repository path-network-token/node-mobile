package network.path.mobilenode.data.http

import kotlinx.coroutines.Deferred
import network.path.mobilenode.domain.entity.AutonomousSystem
import retrofit2.http.GET
import retrofit2.http.Path

interface IpToAutonomousSystemService {
    @GET("v1/as/ip/{ip}")
    fun getAutonomousSystem(@Path("ip") ip: String): Deferred<AutonomousSystem>
}