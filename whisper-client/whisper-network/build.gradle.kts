plugins {
    kotlin("jvm") version "2.1.20"
}

group = "com.whisper"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val ktor_version = "3.1.3"
    implementation("io.ktor:ktor-client-core:${ktor_version}")
    implementation("io.ktor:ktor-client-websockets:${ktor_version}")
    implementation("io.ktor:ktor-client-cio:${ktor_version}") // CIO engine (can also use OkHttp or Darwin for iOS)
    implementation("io.ktor:ktor-client-content-negotiation:${ktor_version}")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}