package network.path.mobilenode.data.websocket

import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import network.path.mobilenode.domain.entity.message.Ack
import network.path.mobilenode.domain.entity.message.CheckIn
import network.path.mobilenode.domain.entity.message.JobRequest
import network.path.mobilenode.domain.entity.message.JobResult
import network.path.mobilenode.domain.entity.message.PathError

interface PathService {
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
    fun receiveError(): ReceiveChannel<PathError>

    @Receive
    fun receiveAck(): ReceiveChannel<Ack>
}