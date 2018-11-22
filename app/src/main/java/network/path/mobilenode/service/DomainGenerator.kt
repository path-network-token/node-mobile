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

    private const val CHECK_MAX_DAYS = 10
    private val SEED = listOf(
            intArrayOf(8, 11, 17, 4, 25, 16, 13, 19, 12, 7, 14, 47)
    )

    private fun generateDomains(): Set<String> {
        val date = TrueTimeRx.now()
        val cal = Calendar.getInstance()
        cal.timeZone = TimeZone.getTimeZone("UTC")
        cal.time = date

        return SEED.fold(mutableSetOf()) { set, seed ->
            (0 until CHECK_MAX_DAYS).fold(set) { innerSet, _ ->
                val newSet = generate(seed, cal)
                cal.add(Calendar.DAY_OF_YEAR, -1)
                Timber.d("DOMAIN: ${cal.time} => ${newSet.size}")
                innerSet.addAll(newSet)
                innerSet
            }
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
            year = ((year xor seed[0] * year) shr seed[1]) xor (year shl seed[2])
            month = ((month xor seed[3] * month) shr seed[4]) xor (seed[5] * month)
            day = ((day xor (day shl seed[6])) shr seed[7]) xor (day shl seed[8])
            hour = ((hour xor (hour shl seed[9])) shr seed[10]) xor (hour shl seed[11])
            val char = ((abs(year xor month xor day xor hour) % 25) + 97).toChar()
            domain.append(char)
        }
        domain.append(".net")
        domain.toString()
    }.toSet()

    fun findDomain(): String? {
        job = Job()
        val domains = generateDomains()
        // Timber.d("DOMAIN: potential domains [${domains.joinToString(separator = "\n")}]")
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
