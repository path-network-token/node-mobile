package network.path.mobilenode.data.http

import com.google.gson.Gson
import kotlinx.coroutines.InternalCoroutinesApi
import network.path.mobilenode.domain.entity.CheckIn
import network.path.mobilenode.domain.entity.JobList
import network.path.mobilenode.domain.entity.JobRequest
import network.path.mobilenode.domain.entity.JobResult
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody

interface PathService {
    suspend fun checkIn(nodeId: String?, checkIn: CheckIn): JobList

    suspend fun requestDetails(executionId: String): JobRequest

    suspend fun postResult(nodeId: String, executionId: String, result: JobResult)

    fun close()
}

@InternalCoroutinesApi
class PathServiceNew(private val baseUrl: String,
                     private val workerPool: OkHttpWorkerPool,
                     private val gson: Gson) : PathService {
    companion object {
        private val JSON = MediaType.parse("application/json; charset=utf-8")
    }

    init {
        workerPool.startWorkers()
    }

    override suspend fun checkIn(nodeId: String?, checkIn: CheckIn): JobList {
        val request = Request.Builder()
                .url("${baseUrl}checkin/$nodeId")
                .post(RequestBody.create(JSON, gson.toJson(checkIn)))
                .build()
        val response = workerPool.execute(request)
        val body = response.getBody()
        return gson.fromJson(body.string(), JobList::class.java)
    }

    override suspend fun requestDetails(executionId: String): JobRequest {
        val request = Request.Builder()
                .url("${baseUrl}job_request/$executionId")
                .build()
        val response = workerPool.execute(request)
        val body = response.getBody()
        return gson.fromJson(body.string(), JobRequest::class.java)
    }

    override suspend fun postResult(nodeId: String, executionId: String, result: JobResult) {
        val request = Request.Builder()
                .url("${baseUrl}job_result/$nodeId/$executionId")
                .post(RequestBody.create(JSON, gson.toJson(result)))
                .build()
        val response = workerPool.execute(request)
        response.getBody()
    }

    override fun close() = workerPool.close()
}
