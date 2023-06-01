package rs.sqpr.companion

import dev.minn.jda.ktx.generics.getChannel
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent

val roamChannelsUnion by lazy { config.channels.roamingVoiceIds.map { guild.getChannel<AudioChannelUnion>(it)!! } }
val roamChannelsVoice by lazy { roamChannelsUnion.map { it.asVoiceChannel() } }
val roamingRole by lazy { guild.getRoleById(config.roles.roaming)!! }
fun GuildVoiceUpdateEvent.isRoamingChannel(): Boolean {
    val left = channelLeft.isRoamingChannel()
    val right = channelJoined.isRoamingChannel()
    return left && right
}

fun AudioChannelUnion?.isRoamingChannel(): Boolean = this?.asVoiceChannel()?.isRoamingChannel() ?: false
fun VoiceChannel.isRoamingChannel(): Boolean = roamChannelsVoice.contains(this)
fun UserSnowflake.isRoaming() = guild.getMember(this)?.roles?.contains(roamingRole) ?: false
fun UserSnowflake.addRoaming() = guild.addRoleToMember(this, roamingRole)
fun UserSnowflake.removeRoaming() = guild.removeRoleFromMember(this, roamingRole)
