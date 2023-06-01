package rs.sqpr.companion.config

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Config(
    val token: String,
    val guildId: String,
    val channels: Channels,
    val roles: Roles
)

@JsonClass(generateAdapter = true)
data class Channels(
    val announcementsId: String,
    val roamingTextId: String,
    val roamingVoiceIds: List<String>
)

@JsonClass(generateAdapter = true)
data class Roles(
    val administration: String,
    val roaming: String,
    val wipe: String,
    val spqr: String
)