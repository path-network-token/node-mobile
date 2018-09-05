package pl.droidsonroids.minertest.websocket

import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import pl.droidsonroids.minertest.message.*

interface MinerService {
    @Send
    fun sendCheckIn(checkIn: CheckIn)

    @Send
    fun sendAck(ack: Ack)

    @Send
    fun sendJobResult(jobResult: JobResult)

    @Receive
    fun receiveJobRequest(): ReceiveChannel<JobRequest>

    @Receive
    fun receiveError(): ReceiveChannel<MinerError>

    @Receive
    fun receiveAck(): ReceiveChannel<Ack>
}