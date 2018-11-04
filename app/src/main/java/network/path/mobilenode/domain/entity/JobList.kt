package network.path.mobilenode.domain.entity

data class JobList(
        val type: String,
        val nodeId: String?,
        val asn: String?,
        val networkPrefix: String?,
        val location: String?,
        val jobs: List<JobExecutionId>
)

data class JobExecutionId(val executionUuid: String)
