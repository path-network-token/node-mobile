package network.path.mobilenode.service

import com.instacart.library.truetime.TrueTimeRx
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.IO
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import timber.log.Timber
import java.net.InetAddress
import java.util.*
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.math.abs

object DomainGenerator : CoroutineScope {
    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private val SEED = listOf(
            intArrayOf(17, 8, 11, 16, 4, 25, 12, 13, 19, 5, 11, 41),
            intArrayOf(8, 11, 17, 4, 25, 16, 13, 19, 12, 7, 14, 47),
            intArrayOf(7, 12, 15, 6, 23, 19, 7, 15, 31, 3, 17, 59)
    )

    private fun generateDomains(): Set<String> {
        val date = TrueTimeRx.now()
        val cal = Calendar.getInstance()
        cal.timeZone = TimeZone.getTimeZone("UTC")
        cal.time = date

        return SEED.fold(mutableSetOf()) { set, seed ->
            set.addAll(generate(seed, cal))
            set
        }
    }

    private fun generate(seed: IntArray, cal: Calendar) = (1..24).map {
        var year = cal.get(Calendar.YEAR).toLong()
        var month = cal.get(Calendar.MONTH).toLong()
        var day = cal.get(Calendar.DAY_OF_MONTH).toLong()
        var hour = it.toLong()
        val domain = StringBuffer("http://")
        for (i in 1..16) {
            year = ((year xor seed[0] * year) shr seed[1]) xor ((year and 0xFFFFFFF0) shl seed[2])
            month = ((month xor seed[3] * month) shr seed[4]) xor seed[5] * (month and 0xFFFFFFF8)
            day = ((day xor (day shl seed[6])) shr seed[7]) xor ((day and 0xFFFFFFFE) shl seed[8])
            hour = ((hour xor seed[9] * hour) shr seed[10]) xor (hour shl seed[11])
            val char = ((abs(year xor month xor day xor hour) % 25) + 97).toChar()
            domain.append(char)
        }
        domain.append(".net")
        domain.toString()
    }

    fun findDomain(): String? {
        job = Job()
        val domains = generateDomains()
        Timber.d("DOMAIN: potential domains count [${domains.size}]")
        val resolved = runBlocking {
            domains.mapNotNull {
                resolve(it).await()
            }
        }
        job.cancel()
        Timber.d("DOMAIN: resolved domains [$resolved]")
        return resolved.firstOrNull()
    }

    private fun resolve(domain: String): Deferred<String?> = async {
        try {
            InetAddress.getByName(domain)
            domain
        } catch (e: Exception) {
            Timber.d("DOMAIN: cannot resolve host [$domain]: $e")
            null
        }
    }
}
