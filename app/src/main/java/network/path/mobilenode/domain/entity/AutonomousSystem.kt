package network.path.mobilenode.domain.entity

data class AutonomousSystem(
        val announced: Boolean,
        val asCountryCode: String?,
        val asDescription: String?,
        val asNumber: String?,
        val firstIp: String?,
        val lastIp: String?
)