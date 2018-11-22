package network.path.mobilenode.domain

import network.path.mobilenode.domain.entity.CheckType
import network.path.mobilenode.domain.entity.CheckTypeStatistics

interface PathStorage {
    var walletAddress: String
    var nodeId: String?
    var isActivated: Boolean

    var proxyDomain: String?

    fun statisticsForType(type: CheckType): CheckTypeStatistics
    fun recordStatistics(type: CheckType, elapsed: Long): CheckTypeStatistics
}
