package network.path.mobilenode.domain.entity

import network.path.mobilenode.BuildConfig

open class CheckIn(
        open val type: String = "checkin",
        val nodeId: String?,
        val lat: String? = null,
        val lon: String? = null,
        val wallet: String,
        val deviceType: String? = "android",
        val pathApiVersion: String = "1.0",
        val nodeBuildVersion: String = BuildConfig.VERSION_NAME,
        val returnJobsMax: Int = 100
) {
    override fun toString(): String =
            "${javaClass.simpleName}($type, $nodeId, $lat, $lon, '$wallet', '$deviceType', '$pathApiVersion', '$nodeBuildVersion', $returnJobsMax)"
}
