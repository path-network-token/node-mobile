package pl.droidsonroids.minertest.service

import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.selects.select
import timber.log.Timber

private const val LAST_LOCATION_TIMEOUT_MILLIS = 1000

class LastLocationProvider(context: Context) {

    private val fusedLocationProvider = LocationServices.getFusedLocationProviderClient(context)

    suspend fun getLastLocationOrNull(): Location? = select {
        createFusedLocationProducer().onReceive { it }
        createFallbackLocationProducer().onReceive { it }
    }

    private fun createFusedLocationProducer(): ReceiveChannel<Location?> {
        val channel = Channel<Location?>(1)
        with(fusedLocationProvider.lastLocation) {
            addOnSuccessListener {
                Timber.v("Last location: $it mocked: ${it.isFromMockProvider}")
                channel.offer(it)
            }
            addOnFailureListener {
                Timber.v("Could not get location: $it")
                channel.offer(null)
            }
        }
        return channel
    }

    private fun createFallbackLocationProducer() = produce<Location?>(capacity = 1) {
        delay(LAST_LOCATION_TIMEOUT_MILLIS)
        offer(null)
    }
}