package com.whisper.plugins

import com.whisper.model.Whisper
import com.whisper.whisper
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        whisper(Whisper())
        get("/") {
            call.respondText("Hello World!")
        }
    }
}
