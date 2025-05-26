package com.whisper

import com.whisper.model.Whisper
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach

fun Route.whisper(whisper: Whisper) {
    route("/voip/{id}") {
        webSocket {
            val id = call.parameters["id"]
            if (id == null) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Missing ID"))
                return@webSocket
            }

            whisper.connectClient(this, id)

            try {
                incoming.consumeEach { frame ->
                    if (frame is Frame.Binary) {
                        val bytes = frame.readBytes()
                        whisper.saveMessage(id, bytes)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                whisper.disconnectClient(id)
                whisper.saveToFile()
            }
        }

        get {
            val id = call.parameters["id"]
            call.respondText(id ?: "Empty id")
        }
    }
}
