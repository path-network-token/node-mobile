package network.path.mobilenode.data.websocket

import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import network.path.mobilenode.data.websocket.message.Ack
import network.path.mobilenode.data.websocket.message.CheckIn
import network.path.mobilenode.data.websocket.message.PathError
import network.path.mobilenode.data.websocket.message.SocketJobRequest
import network.path.mobilenode.data.websocket.message.SocketJobResult

interface PathService {
    @Receive
    fun receiveWebSocketEvent(): ReceiveChannel<WebSocket.Event>

    @Send
    fun sendCheckIn(checkIn: CheckIn)

    @Send
    fun sendAck(ack: Ack)

    @Send
    fun sendJobResult(jobResult: SocketJobResult)

    @Receive
    fun receiveJobRequest(): ReceiveChannel<SocketJobRequest>

    @Receive
    fun receiveError(): ReceiveChannel<PathError>

    @Receive
    fun receiveAck(): ReceiveChannel<Ack>
}