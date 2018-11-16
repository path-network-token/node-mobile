package network.path.mobilenode.data.http

import com.google.gson.Gson
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.IO
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import network.path.mobilenode.domain.PathExternalServices
import network.path.mobilenode.domain.entity.AutonomousSystem
import network.path.mobilenode.service.NetworkMonitor
import okhttp3.OkHttpClient
import java.io.IOException
import kotlin.coroutines.experimental.CoroutineContext

class PathExternalServicesImpl(
        private val networkMonitor: NetworkMonitor,
        okHttpClient: OkHttpClient,
        gson: Gson
) : PathExternalServices, CoroutineScope {
    private val job = Job()

    private val autonomousSystemDetailsDownloader = AutonomousSystemDetailsDownloader(okHttpClient, gson)
    private val externalIpAddressDownloader = ExternalIpAddressDownloader(okHttpClient)

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override val ip = ConflatedBroadcastChannel<String?>()

    override val details = ConflatedBroadcastChannel<AutonomousSystem?>()

    init {
        registerDetailsHandler()
        registerNetworkHandler()
    }

    override fun start() {
    }

    override fun stop() {
        job.cancel()
    }

    private fun retrieveIp() = launch {
        val externalIpAddress = getExternalIpOrNull()
        ip.send(externalIpAddress)
    }

    private fun registerDetailsHandler() = launch {
        ip.openSubscription().consumeEach { ipAddress ->
            val asDetails = ipAddress?.let { getAutonomousSystemOrNull(it) }
            details.send(asDetails)
        }
    }

    private fun registerNetworkHandler() = launch {
        networkMonitor.connected.consumeEach {
            retrieveIp()
        }
    }

    private suspend fun getAutonomousSystemOrNull(externalIpAddress: String) = try {
        autonomousSystemDetailsDownloader.getAutonomousSystem(externalIpAddress)
                .await()
                .run {
                    if (announced) this else null
                }
    } catch (e: IOException) {
        null
    }

    private suspend fun getExternalIpOrNull() = try {
        externalIpAddressDownloader.getExternalIp()
                .await()
                .trimEnd()
    } catch (e: IOException) {
        null
    }
}
