package network.path.mobilenode.domain.entity

import com.google.gson.annotations.SerializedName

data class JobList(
        val type: String,
        val nodeId: String?,
        @SerializedName("ASN")
        val asn: String?,
        val networkPrefix: String?,
        val location: String?,
        val jobs: List<JobExecutionId>
)

data class JobExecutionId(val executionUuid: String)