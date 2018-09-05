package pl.droidsonroids.minertest.websocket

import com.google.gson.Gson
import com.tinder.scarlet.Message
import com.tinder.scarlet.MessageAdapter
import com.tinder.scarlet.messageadapter.gson.GsonMessageAdapter
import pl.droidsonroids.minertest.message.MinerMessage
import java.io.IOException
import java.lang.reflect.Type

class MinerGsonMessageAdapter<T>(private val wrappedAdapter: MessageAdapter<T>) : MessageAdapter<T> by wrappedAdapter {
    override fun fromMessage(message: Message): T {
        val value = wrappedAdapter.fromMessage(message)
        return when {
            value is MinerMessage && value.type.messageClass == value.javaClass -> value
            else -> throw IOException("Unsupported value: $value")
        }
    }

    class Factory(gson: Gson) : MessageAdapter.Factory {

        private val wrappedFactory = GsonMessageAdapter.Factory(gson)

        override fun create(type: Type, annotations: Array<Annotation>) =
            MinerGsonMessageAdapter(wrappedFactory.create(type, annotations))
    }
}