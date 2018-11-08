package network.path.mobilenode.domain.entity

data class JobResult(
        val type: String = "job-result",
        val checkType: CheckType,
        val executionUuid: String,
        val status: String,
        val responseTime: Long,
        val responseBody: String,
        val contentLength: Int = responseBody.length
)
