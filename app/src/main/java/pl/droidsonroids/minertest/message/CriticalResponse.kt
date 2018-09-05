package pl.droidsonroids.minertest.message

data class CriticalResponse(
    val headerStatus: String,
    val bodyContains: String
)