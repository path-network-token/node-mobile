package pl.droidsonroids.minertest

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import pl.droidsonroids.minertest.message.Ack
import pl.droidsonroids.minertest.message.CheckIn
import pl.droidsonroids.minertest.message.JobRequest
import pl.droidsonroids.minertest.message.JobResult
import pl.droidsonroids.minertest.websocket.MinerService
import java.io.IOException
import java.util.*

private const val HEARTBEAT_INTERVAL_MILLIS = 30_000L

class Miner(
    private val job: Job,
    private val storage: Storage,
    private val minerService: MinerService
) {
    private val jobRunners = mapOf(
        "http" to HttpRunner()
    )

    init {
        registerJobRequestHandler()
        registerErrorHandler()
        registerAckHandler()
    }

    private fun registerJobRequestHandler() = launchInBackground {
        minerService.receiveJobRequest().consumeEach { jobRequest ->
            println("job request from server: $jobRequest")
            val ack = Ack(id = jobRequest.id, minerId = null)

            minerService.sendAck(ack)
            println("job request ack sent: $ack")

            val runner = jobRunners.getOrElse(jobRequest.protocol) { TODO() }
            val jobResult = runner.runJob(jobRequest)
            minerService.sendJobResult(jobResult)
            println("job result sent: $jobResult")
        }
    }

    private fun registerErrorHandler() = launchInBackground {
        minerService.receiveError().consumeEach {
            println("error from server: $it")
        }
    }

    private fun registerAckHandler() = launchInBackground {
        minerService.receiveAck().consumeEach {
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

interface JobRunner {
    fun runJob(jobRequest: JobRequest): JobResult
}

class HttpRunner : JobRunner {
    private val client = OkHttpClient()

    override fun runJob(jobRequest: JobRequest): JobResult {
        val url = HttpUrl.Builder()
            .host(jobRequest.endpointAddress)
            .port(jobRequest.endpointPort)
            .scheme("http")
            .build()

        val request = Request.Builder()
            .method(jobRequest.method.name, null)
            .url("$url/${jobRequest.endpointAdditionalParams}")
            .build()

        try {
            val response = client.newCall(request)
                .execute()
            TODO()
        } catch (e:IOException){
            TODO()
        }
        return JobResult(jobUuid = jobRequest.jobUuid, responseTime = TODO(), responseBody = TODO(), status = TODO())
    }
}
