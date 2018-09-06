package pl.droidsonroids.minertest

import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ConflatedChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.filter
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import pl.droidsonroids.minertest.message.Ack
import pl.droidsonroids.minertest.message.CheckIn
import pl.droidsonroids.minertest.message.JobRequest
import pl.droidsonroids.minertest.runner.Runner
import pl.droidsonroids.minertest.runner.getRunner
import pl.droidsonroids.minertest.websocket.WebSocketClient
import timber.log.Timber

private const val HEARTBEAT_INTERVAL_MILLIS = 30_000L
private const val RECONNECT_DELAY = 37_000L

class Miner(
    private val job: Job,
    private val storage: Storage
) {
    private val webSocketClient = WebSocketClient(job)
    private val minerService = webSocketClient.minerService
    private var timeoutJob = Job()

    private val _jobCompleteReceiveChannel = ConflatedChannel<Long>()
    val jobCompleteReceiveChannel: ReceiveChannel<Long> = _jobCompleteReceiveChannel

    init {
        resetWatchdog()
        registerJobRequestHandler()
        registerErrorHandler()
        registerAckHandler()
        registerStartHandler()
        registerWatchdogResetHandler()
    }

    fun start() {
        webSocketClient.connect()
    }

    private fun registerStartHandler() = launchInBackground {
        minerService.receiveWebSocketEvent()
            .filter { it is WebSocket.Event.OnConnectionOpened<*> }
            .consumeEach {
                sendHeartbeat(HEARTBEAT_INTERVAL_MILLIS)
            }
    }

    private fun registerJobRequestHandler() = launchInBackground {
        minerService.receiveJobRequest().consumeEach { jobRequest ->
            Timber.d("job request from server: $jobRequest")
            sendAck(jobRequest)
            val runner = getRunner(jobRequest)
            if (runner != null) {
                runJob(runner, jobRequest)
                dispatchCompletedJobCount()
            } else {
                Timber.d("no runner found for $jobRequest")
            }
        }
    }

    private fun runJob(runner: Runner, jobRequest: JobRequest) {
        val jobResult = runner.runJob(jobRequest)
        minerService.sendJobResult(jobResult)
        Timber.v("job result sent: $jobResult")
    }

    private fun dispatchCompletedJobCount() {
        storage.completedJobsCount++
        _jobCompleteReceiveChannel.offer(storage.completedJobsCount)
    }

    private fun sendAck(jobRequest: JobRequest) {
        val ack = Ack(id = jobRequest.id, minerId = storage.minerId)
        minerService.sendAck(ack)
        Timber.d("ack sent: $ack")
    }

    private fun registerErrorHandler() = launchInBackground {
        minerService.receiveError().consumeEach {
            Timber.w("error from server: $it")
        }
    }

    private fun registerAckHandler() = launchInBackground {
        minerService.receiveAck().consumeEach {
            Timber.d("ack from server: $it")
            storage.minerId = it.minerId
        }
    }

    private fun registerWatchdogResetHandler() = launchInBackground {
        minerService.receiveWebSocketEvent()
            .filter { it is WebSocket.Event.OnMessageReceived }
            .consumeEach {
                resetWatchdog()
            }
    }

    private fun resetWatchdog() {
        timeoutJob.cancel()
        timeoutJob = launchInBackground {
            delay(RECONNECT_DELAY)
            Timber.w("watchdog detected timeout")
            webSocketClient.reconnect()
        }
    }

    private tailrec suspend fun sendHeartbeat(intervalMillis: Long) {
        val wallet = storage.pathWalletAddress ?: throw IllegalStateException("Missing wallet address")
        val checkIn = CheckIn(minerId = null, wallet = wallet)

        Timber.d("client check in: $checkIn")

        minerService.sendCheckIn(checkIn)
        delay(intervalMillis)
        sendHeartbeat(intervalMillis)
    }

    private fun launchInBackground(block: suspend () -> Unit): Job {
        return launch(parent = job) { block() }
    }
}
