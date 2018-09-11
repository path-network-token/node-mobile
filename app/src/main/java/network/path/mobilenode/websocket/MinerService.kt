package network.path.mobilenode.websocket

import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import network.path.mobilenode.message.*

interface MinerService {
    @Receive
    fun receiveWebSocketEvent(): ReceiveChannel<WebSocket.Event>

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