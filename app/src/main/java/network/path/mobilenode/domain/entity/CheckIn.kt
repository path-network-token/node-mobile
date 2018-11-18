package network.path.mobilenode.domain.entity

import network.path.mobilenode.BuildConfig
import network.path.mobilenode.Constants

data class CheckIn(
        val type: String = "checkin",
        val nodeId: String?,
        val lat: String? = null,
        val lon: String? = null,
        val wallet: String,
        val deviceType: String? = "android",
        val pathApiVersion: String = Constants.PATH_API_VERSION,
        val nodeBuildVersion: String = BuildConfig.VERSION_NAME,
        val returnJobsMax: Int = 10
)
