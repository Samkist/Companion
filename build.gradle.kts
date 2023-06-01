plugins {
    kotlin("jvm") version "1.8.21"
    kotlin("kapt") version "1.8.21"
    application
}

group = "rs.spqr"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
    // For JDA-Chewtils
    maven("https://m2.chew.pro/snapshots")
}

dependencies {
    testImplementation(kotlin("test"))
    // https://mvnrepository.com/artifact/com.squareup.moshi/moshi-kotlin
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")
    implementation("ch.qos.logback:logback-classic:1.4.7")
    implementation("net.dv8tion:JDA:5.0.0-beta.9")
    implementation("com.github.minndevelopment:jda-ktx:9370cb1")
    implementation("io.github.oshai:kotlin-logging-jvm:4.0.0-beta-29")
    implementation("pw.chew:jda-chewtils:2.0-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}