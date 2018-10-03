package network.path.mobilenode.service

import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.experimental.tasks.await
import timber.log.Timber

class LastLocationProvider(context: Context) {

    private val fusedLocationProvider = LocationServices.getFusedLocationProviderClient(context)

    suspend fun getLastRealLocationOrNull(): Location? {
        return try {
            val location = fusedLocationProvider.lastLocation.await()
            Timber.v("$location, mocked: ${location?.isFromMockProvider}")

            return if (location?.isFromMockProvider == true) null else location
        } catch (e: SecurityException) {
            Timber.v(e)
            null
        }
    }
}