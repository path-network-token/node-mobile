package network.path.mobilenode.data.json

import com.google.gson.Gson
import com.tinder.scarlet.Message
import com.tinder.scarlet.MessageAdapter
import com.tinder.scarlet.messageadapter.gson.GsonMessageAdapter
import network.path.mobilenode.data.websocket.message.Ack
import network.path.mobilenode.data.websocket.message.PathError
import network.path.mobilenode.data.websocket.message.PathMessage
import network.path.mobilenode.data.websocket.message.SocketCheckIn
import network.path.mobilenode.data.websocket.message.SocketJobRequest
import network.path.mobilenode.data.websocket.message.SocketJobResult
import java.io.IOException
import java.lang.reflect.Type

class PathGsonMessageAdapter<T>(private val wrappedAdapter: MessageAdapter<T>) : MessageAdapter<T> by wrappedAdapter {
    override fun fromMessage(message: Message): T {
        val value = wrappedAdapter.fromMessage(message)
        return when {
            value is PathMessage && value.isTypeOf(value.type) -> value
            else -> throw IOException("Unsupported value: $value")
        }
    }

    class Factory(gson: Gson) : MessageAdapter.Factory {

        private val wrappedFactory = GsonMessageAdapter.Factory(gson)

        override fun create(type: Type, annotations: Array<Annotation>) =
            PathGsonMessageAdapter(wrappedFactory.create(type, annotations))
    }
}

private val messageTypeNames = mapOf(
    SocketCheckIn::class to MessageType.CHECK_IN,
    Ack::class to MessageType.ACK,
    PathError::class to MessageType.ERROR,
    SocketJobRequest::class to MessageType.JOB_REQUEST,
    SocketJobResult::class to MessageType.JOB_RESULT
)

private fun PathMessage.isTypeOf(type: String?) = type == messageTypeNames[this::class]