package rs.sqpr.companion

import dev.minn.jda.ktx.events.CoroutineEventManager
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.interactions.commands.restrict
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.interactions.commands.subcommand
import dev.minn.jda.ktx.interactions.commands.updateCommands
import dev.minn.jda.ktx.jdabuilder.light
import dev.minn.jda.ktx.messages.Embed
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.events.guild.GenericGuildEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.events.session.ShutdownEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import rs.sqpr.companion.config.Config
import rs.sqpr.companion.config.ConfigManager
import java.util.concurrent.CancellationException
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.math.max
import kotlin.time.Duration.Companion.minutes

/* Logging */
private val logger = KotlinLogging.logger {}

/* Configuration */
lateinit var config: Config

/* Discord API */
private lateinit var jda: JDA
private val intentsList = listOf(
    GatewayIntent.GUILD_PRESENCES,
    GatewayIntent.GUILD_MEMBERS,
    GatewayIntent.GUILD_INVITES,
    GatewayIntent.GUILD_MODERATION,
    GatewayIntent.GUILD_VOICE_STATES,
    GatewayIntent.DIRECT_MESSAGES,
    GatewayIntent.DIRECT_MESSAGE_REACTIONS
)
private val cacheFlags = listOf(
    CacheFlag.ACTIVITY,
    CacheFlag.CLIENT_STATUS,
    CacheFlag.VOICE_STATE,
    CacheFlag.ROLE_TAGS
)
private val memberCachePolicy = MemberCachePolicy.ALL

val guild by lazy { jda.getGuildById(config.guildId)!! }

fun getThreadCount(): Int = max(2, ForkJoinPool.getCommonPoolParallelism())

private val pool = Executors.newScheduledThreadPool(getThreadCount()) {
    thread(start = false, name = "Worker-Thread", isDaemon = true, block = it::run)
}

fun main(args: Array<String>) {

    val dispatcher = pool.asCoroutineDispatcher()
    val supervisor = SupervisorJob()
    val handler = CoroutineExceptionHandler { _, throwable ->
        if (throwable !is CancellationException) {
            logger.error("Uncaught exception in coroutine", throwable)
        }
        if (throwable is Error) {
            supervisor.cancel()
            throw throwable
        }
    }

    val context = dispatcher + supervisor + handler
    val scope = CoroutineScope(context)
    val manager = CoroutineEventManager(scope, 1.minutes)
    manager.initCommands()
    manager.listener<ShutdownEvent> {
        supervisor.cancel()
    }
    manager.listener<ReadyEvent> {
        logger.info { "Companion Connected & Ready" }
    }

    try {
        ConfigManager.initialize()
    } catch (e: Exception) {
        logger.error { "Critical error while initializing configuration" }
        logger.error { e.message }
        return
    }
    config = ConfigManager.config
    logger.info { "Initializing Discord API connection" }
    RestAction.setDefaultTimeout(10, TimeUnit.SECONDS)
    jda = light(config.token, enableCoroutines = false, intents = intentsList) {
        setEventManager(manager)
        setGatewayPool(pool)
        setCallbackPool(pool)
        setRateLimitPool(pool)
        enableCache(cacheFlags)
        setMemberCachePolicy(memberCachePolicy)
    }

    jda.listener<GuildVoiceUpdateEvent> { event ->
        if (event.isRoamingChannel()) {
            val user = UserSnowflake.fromId(event.member.id)
            event.channelLeft?.run {
                user.removeRoaming().queue()
            }

            event.channelJoined?.run {
                user.addRoaming().queue()
            }
        }
    }

    jda.onCommand("admin refresh") { event ->
        val roamChannelMembers = roamChannelsUnion.flatMap { it.members }
        val toRoam = roamChannelMembers.filter { !it.isRoaming() }
        val names = toRoam.joinToString(separator = ",") { it.asMention }
        logger.info { names }
        toRoam.forEach {
            it.addRoaming().queue()
        }

        event.reply()
    }

    jda.awaitReady()
}

private fun CoroutineEventManager.initCommands() = listener<GenericGuildEvent> { event ->
    if (event !is GuildReadyEvent && event !is GuildJoinEvent) return@listener
    val guild = event.guild

    if (!filterId(guild, config.guildId)) return@listener

    guild.updateCommands {
        slash("admin", "Admin Commands") {
            restrict(guild = true, Permission.ADMINISTRATOR)
            subcommand("refresh", "Refresh the roles in roaming channels") {

            }
        }


    }.queue()
}
