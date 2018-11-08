package network.path.mobilenode.domain.entity

data class CheckTypeStatistics(val count: Long, val averageLatencyMillis: Long) {
    fun add(latency: Long): CheckTypeStatistics {
        val latencySum = count * averageLatencyMillis
        val newCount = count + 1
        return CheckTypeStatistics(newCount, (latencySum + latency) / newCount)
    }
}
