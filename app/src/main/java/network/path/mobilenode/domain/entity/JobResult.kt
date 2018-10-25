package network.path.mobilenode.domain.entity

open class JobResult(
        val checkType: CheckType,
        val executionUuid: String,
        val status: String,
        val responseTime: Long,
        val responseBody: String,
        val contentLength: Int = responseBody.length
)
