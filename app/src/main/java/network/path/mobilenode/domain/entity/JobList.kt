package network.path.mobilenode.domain.entity

class JobList(
        val type: String,
        val nodeId: String?,
        val jobs: List<JobExecutionId>
)

class JobExecutionId(val executionId: String)