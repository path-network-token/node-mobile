package pl.droidsonroids.minertest

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.single
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import pl.droidsonroids.minertest.message.Ack
import pl.droidsonroids.minertest.message.CheckIn
import pl.droidsonroids.minertest.message.JobRequest
import pl.droidsonroids.minertest.message.Status
import pl.droidsonroids.minertest.runner.getRunner
import pl.droidsonroids.minertest.websocket.MinerService

private const val HEARTBEAT_INTERVAL_MILLIS = 30_000L

class Miner(
    private val job: Job,
    private val storage: Storage,
    private val minerService: MinerService,
    private val onJobCompleted: () -> Unit
) {

    init {
        registerJobRequestHandler()
        registerErrorHandler()
        registerAckHandler()
    }

    private fun registerJobRequestHandler() = launchInBackground {
        minerService.receiveJobRequest().consumeEach { jobRequest ->
            println("job request from server: $jobRequest")

            sendAck(jobRequest)

            val runner = getRunner(jobRequest)
            val jobResult = runner.runJob(jobRequest)
            minerService.sendJobResult(jobResult)
            if (jobResult.status == Status.ok) onJobCompleted()
            println("job result sent: $jobResult")
        }
    }

    private fun sendAck(jobRequest: JobRequest) {
        val ack = Ack(id = jobRequest.id, minerId = null)
        minerService.sendAck(ack)
        println("ack sent: $ack")
    }

    private fun registerErrorHandler() = launchInBackground {
        minerService.receiveError().consumeEach {
            println("error from server: $it")
        }
    }

    private fun registerAckHandler() = launchInBackground {
        minerService.receiveAck().consumeEach {
            //TODO reconnect if there is no ack nor error received for 30s
            println("ack from server: $it")
        }
    }

    fun startHeartbeat() = launchInBackground {
        sendHeartbeat(HEARTBEAT_INTERVAL_MILLIS)
    }

    private tailrec suspend fun sendHeartbeat(intervalMillis: Long) {
        val wallet = storage.pathWalletAddress ?: throw IllegalStateException("Missing wallet address")
        val checkIn = CheckIn(minerId = null, wallet = wallet)

        println("client check in: $checkIn")

        minerService.sendCheckIn(checkIn)
        delay(intervalMillis)
        sendHeartbeat(intervalMillis)
    }

    private fun launchInBackground(block: suspend () -> Unit) {
        launch(parent = job) { block() }
    }
}