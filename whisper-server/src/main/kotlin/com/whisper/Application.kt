package com.whisper

import com.whisper.plugins.configureRouting
import com.whisper.plugins.configureSockets
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSockets()
    configureRouting()
}
