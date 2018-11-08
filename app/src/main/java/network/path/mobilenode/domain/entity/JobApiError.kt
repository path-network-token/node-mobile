package network.path.mobilenode.domain.entity

data class JobApiError(val type: String,
                       val errorCode: Int,
                       val description: String)
