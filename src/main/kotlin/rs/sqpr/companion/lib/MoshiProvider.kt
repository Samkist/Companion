package rs.sqpr.companion.lib

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object MoshiProvider {
    fun buildMoshi(): Moshi = Moshi
        .Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
}