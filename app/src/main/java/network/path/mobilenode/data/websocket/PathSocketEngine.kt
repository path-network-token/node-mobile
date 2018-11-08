package network.path.mobilenode.data.websocket

import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.IO
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.filter
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import network.path.mobilenode.data.websocket.message.Ack
import network.path.mobilenode.data.websocket.message.SocketCheckIn
import network.path.mobilenode.data.websocket.message.SocketJobRequest
import network.path.mobilenode.data.websocket.message.SocketJobResult
import network.path.mobilenode.domain.PathEngine
import network.path.mobilenode.domain.PathStorage
import network.path.mobilenode.domain.entity.ConnectionStatus
import network.path.mobilenode.domain.entity.JobList
import network.path.mobilenode.domain.entity.JobRequest
import network.path.mobilenode.domain.entity.JobResult
import network.path.mobilenode.service.LastLocationProvider
import timber.log.Timber
import kotlin.coroutines.experimental.CoroutineContext

class PathSocketEngine(
        private val job: Job,
        private val lastLocationProvider: LastLocationProvider,
        private val webSocketClient: WebSocketClient,
        private val storage: PathStorage
) : PathEngine, CoroutineScope {

    companion object {
        private const val HEARTBEAT_INTERVAL_MILLIS = 30_000L
        private const val RECONNECT_DELAY_MILLIS = 37_000L
    }

    private val pathService = webSocketClient.pathService
    private var timeoutJob = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override val status = ConflatedBroadcastChannel(ConnectionStatus.DISCONNECTED)

    override val requests = ConflatedBroadcastChannel<JobRequest>()

    override val nodeId = ConflatedBroadcastChannel(storage.nodeId)

    override val jobList = ConflatedBroadcastChannel<JobList>()

    init {
        resetWatchdog()
        registerJobRequestHandler()
        registerErrorHandler()
        registerAckHandler()
        registerConnectionOpenedHandler()
        registerWatchdogResetHandler()
    }

    override fun start() {
        webSocketClient.connect()
    }

    override fun processResult(result: JobResult) {
        pathService.sendJobResult(SocketJobResult(result))
    }

    override fun stop() {
        job.cancel()
    }

    private fun registerConnectionOpenedHandler() = launch {
        pathService.receiveWebSocketEvent()
                .filter { it is WebSocket.Event.OnConnectionOpened<*> }
                .consumeEach {
                    sendHeartbeat(HEARTBEAT_INTERVAL_MILLIS)
                }
    }

    private fun registerJobRequestHandler() = launch {
        pathService.receiveJobRequest().consumeEach { request ->
            Timber.d("Request received [$request]")
            sendAck(request)
            requests.send(request.jobRequest())
        }
    }

    private fun sendAck(jobRequest: SocketJobRequest) {
        val ack = Ack(id = jobRequest.id)
        pathService.sendAck(ack)
        Timber.d("ACK sent [$ack]")
    }

    private fun registerErrorHandler() = launch {
        pathService.receiveError().consumeEach {
            Timber.w("Server error [$it]")
        }
    }

    private fun registerAckHandler() = launch {
        pathService.receiveAck().consumeEach {
            status.send(ConnectionStatus.CONNECTED)
            Timber.d("ACK received [$it]")
            if (it.nodeId != null) {
                nodeId.send(it.nodeId)
            }
        }
    }

    private fun registerWatchdogResetHandler() = launch {
        pathService.receiveWebSocketEvent()
                .filter { it is WebSocket.Event.OnMessageReceived }
                .consumeEach {
                    status.send(ConnectionStatus.CONNECTED)
                    resetWatchdog()
                }
    }

    private fun resetWatchdog() {
        Timber.d("Watchdog reset")
        timeoutJob.cancel()
        timeoutJob = launch {
            delay(RECONNECT_DELAY_MILLIS)
            status.send(ConnectionStatus.DISCONNECTED)
            Timber.w("Watchdog timeout")
            webSocketClient.reconnect()
            resetWatchdog()
        }
    }

    private tailrec suspend fun sendHeartbeat(intervalMillis: Long) {
        val checkIn = createCheckInMessage()
        Timber.d("Check in [$checkIn]")

        pathService.sendCheckIn(checkIn)
        delay(intervalMillis)
        sendHeartbeat(intervalMillis)
    }

    private suspend fun createCheckInMessage(): SocketCheckIn {
        val location = lastLocationProvider.getLastRealLocationOrNull()
        return SocketCheckIn(
                nodeId = storage.nodeId,
                wallet = storage.walletAddress,
                lat = location?.latitude?.toString(),
                lon = location?.longitude?.toString()
        )
    }
}
