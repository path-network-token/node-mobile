package pl.droidsonroids.minertest.message

data class JobRequest(
    override val id: String,
    override val type: MessageType,
    val protocol: String,
    val method: Method?,
    val headers: List<Map<String, String>>?,
    val payload: String?,
    val endpointAddress: String,
    val endpointPort: Int?,
    val endpointAdditionalParams: String?,
    val degradedAfter: Long?,
    val criticalAfter: Long?,
    val jobUuid: String
) : MinerMessage