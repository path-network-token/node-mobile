package network.path.mobilenode.data.http

import kotlinx.coroutines.Deferred
import network.path.mobilenode.domain.entity.CheckIn
import network.path.mobilenode.domain.entity.JobList
import network.path.mobilenode.domain.entity.JobRequest
import network.path.mobilenode.domain.entity.JobResult
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PathService {
    @POST("/checkin/{nodeId}")
    fun checkIn(@Path("nodeId") nodeId: String?, @Body checkIn: CheckIn): Deferred<JobList>

    @GET("/job_request/{executionId}")
    fun requestDetails(@Path("executionId") executionId: String): Deferred<JobRequest>

    @POST("/job_result/{nodeId}/{executionId}")
    fun postResult(@Path("nodeId") nodeId: String, @Path("executionId") executionId: String, @Body result: JobResult): Deferred<Unit>
}
