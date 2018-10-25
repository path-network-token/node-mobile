package network.path.mobilenode.domain.entity

open class JobResult(
        open val type: String = "job-result",
        val checkType: CheckType,
        val executionUuid: String,
        val status: String,
        val responseTime: Long,
        val responseBody: String,
        val contentLength: Int = responseBody.length
) {
    override fun toString(): String =
            "${javaClass.simpleName}($type, $checkType, $executionUuid, $status, $responseTime, $responseBody, $contentLength)"
}
