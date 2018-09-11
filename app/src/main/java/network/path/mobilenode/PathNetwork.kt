package network.path.mobilenode

import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.filter
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import network.path.mobilenode.info.ConnectionStatus.CONNECTED
import network.path.mobilenode.info.ConnectionStatus.DISCONNECTED
import network.path.mobilenode.message.Ack
import network.path.mobilenode.message.CheckIn
import network.path.mobilenode.message.JobRequest
import network.path.mobilenode.runner.Runner
import network.path.mobilenode.runner.getRunner
import network.path.mobilenode.service.LastLocationProvider
import network.path.mobilenode.websocket.WebSocketClient
import timber.log.Timber

private const val HEARTBEAT_INTERVAL_MILLIS = 30_000L
private const val RECONNECT_DELAY_MILLIS = 37_000L

class PathNetwork(
    private val job: Job,
    private val storage: Storage,
    private val lastLocationProvider: LastLocationProvider
) {
    private val webSocketClient = WebSocketClient(job)
    private val pathService = webSocketClient.pathService
    private var timeoutJob = Job()

    private val jobCompletedChannel = ConflatedBroadcastChannel(storage.completedJobsCount)
    private val connectionStatusChannel = ConflatedBroadcastChannel(DISCONNECTED)

    fun receiveJobCompleted() = jobCompletedChannel.openSubscription()
    fun receiveConnectionStatus() = connectionStatusChannel.openSubscription()

    init {
        resetWatchdog()
        registerJobRequestHandler()
        registerErrorHandler()
        registerAckHandler()
        registerConnectionOpenedHandler()
        registerWatchdogResetHandler()
    }

    fun start() = webSocketClient.connect()

    fun finish() {
        connectionStatusChannel.offer(DISCONNECTED)
    }

    private fun registerConnectionOpenedHandler() = launchInBackground {
        pathService.receiveWebSocketEvent()
            .filter { it is WebSocket.Event.OnConnectionOpened<*> }
            .consumeEach {
                connectionStatusChannel.offer(CONNECTED)
                sendHeartbeat(HEARTBEAT_INTERVAL_MILLIS)
            }
    }

    private fun registerJobRequestHandler() = launchInBackground {
        pathService.receiveJobRequest().consumeEach { jobRequest ->
            Timber.d("job request from server: $jobRequest")
            sendAck(jobRequest)
            val runner = jobRequest.getRunner()
            runJob(runner, jobRequest)
            dispatchCompletedJobCount()
        }
    }

    private suspend fun runJob(runner: Runner, jobRequest: JobRequest) {
        val jobResult = runner.runJob(jobRequest)
        pathService.sendJobResult(jobResult)
        Timber.v("job result sent: $jobResult")
    }

    private fun dispatchCompletedJobCount() {
        storage.completedJobsCount++
        jobCompletedChannel.offer(storage.completedJobsCount)
    }

    private fun sendAck(jobRequest: JobRequest) {
        val ack = Ack(id = jobRequest.id)
        pathService.sendAck(ack)
        Timber.d("ack sent: $ack")
    }

    private fun registerErrorHandler() = launchInBackground {
        pathService.receiveError().consumeEach {
            Timber.w("error from server: $it")
        }
    }

    private fun registerAckHandler() = launchInBackground {
        pathService.receiveAck().consumeEach {
            Timber.d("ack from server: $it")
            storage.nodeId = it.nodeId
        }
    }

    private fun registerWatchdogResetHandler() = launchInBackground {
        pathService.receiveWebSocketEvent()
            .filter { it is WebSocket.Event.OnMessageReceived }
            .consumeEach {
                resetWatchdog()
            }
    }

    private fun resetWatchdog() {
        Timber.w("watchdog reset")
        timeoutJob.cancel()
        timeoutJob = launchInBackground {
            delay(RECONNECT_DELAY_MILLIS)
            connectionStatusChannel.offer(DISCONNECTED)
            Timber.w("watchdog detected timeout")
            webSocketClient.reconnect()
        }
    }

    private tailrec suspend fun sendHeartbeat(intervalMillis: Long) {
        val checkIn = createCheckInMessage()
        Timber.d("client check in: $checkIn")

        pathService.sendCheckIn(checkIn)
        delay(intervalMillis)
        sendHeartbeat(intervalMillis)
    }

    private suspend fun createCheckInMessage(): CheckIn {
        val lastLocation = lastLocationProvider.getLastLocationOrNull()
        return if (lastLocation != null && !lastLocation.isFromMockProvider) {
            CheckIn(
                nodeId = storage.nodeId,
                wallet = storage.pathWalletAddress,
                lat = lastLocation.latitude.toString(),
                lon = lastLocation.longitude.toString()
            )
        } else {
            CheckIn(
                nodeId = storage.nodeId,
                wallet = storage.pathWalletAddress
            )
        }
    }

    private fun launchInBackground(block: suspend () -> Unit): Job {
        return launch(parent = job) { block() }
    }
}