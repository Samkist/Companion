package rs.sqpr.companion

import dev.minn.jda.ktx.jdabuilder.scope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import kotlin.time.Duration

fun filterId(guild: Guild, id: Long) = guild.idLong == id || id == 0L

fun filterId(guild: Guild, id: String) = guild.id == id
inline fun JDA.repeatUntilShutdown(rate: Duration, initDelay: Duration = rate, crossinline task: suspend CoroutineScope.() -> Unit): Job {
    return scope.launch {
        delay(initDelay)
        while (status != JDA.Status.SHUTDOWN) {
            task()
            delay(rate)
        }
    }
}