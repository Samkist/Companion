package rs.sqpr.companion.config

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import rs.sqpr.companion.lib.MoshiProvider
import java.io.*

object ConfigManager {
    private const val configFile = "config.json"
    lateinit var config: Config
    private lateinit var moshi: Moshi

    @OptIn(ExperimentalStdlibApi::class)
    fun initialize() {
        val file = File(configFile)
        val reader = BufferedReader(FileReader(file))
        val lineSeparator = System.getProperty("line.separator") ?: "\n"
        val builder = StringBuilder()
        var line = reader.readLine()
        while (line != null) {
            builder.append(line)
            builder.append(lineSeparator)
            line = reader.readLine()
        }
        builder.deleteCharAt(builder.length - 1)
        reader.close()
        val json = builder.toString()
        moshi = MoshiProvider.buildMoshi()
        val adapter: JsonAdapter<Config> = moshi.adapter<Config>()
        adapter.fromJson(json)?.let {
            config = it
        } ?: run {
            throw JsonEncodingException("Failed to decode $configFile")
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun writeConfig(config: Config) {
        val file = File(configFile)
        val writer = BufferedWriter(FileWriter(file))
        moshi = MoshiProvider.buildMoshi()
        val adapter: JsonAdapter<Config> = moshi.adapter<Config>().indent("    ")
        adapter.toJson(config)?.let {
            writer.write(it)
            writer.close()
        } ?: run {
            writer.close()
            throw JsonEncodingException("Failed to encode $configFile")
        }
    }
}