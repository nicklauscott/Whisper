package com.whisper.model

import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.filterKeys
import kotlin.collections.forEach
import kotlin.collections.mutableListOf
import kotlin.collections.set

class Whisper {
    private val clients = ConcurrentHashMap<String, WebSocketSession>()
    private val receivedBytes = mutableListOf<ByteArray>()
    private val logger = LoggerFactory.getLogger(Whisper::class.java)

    fun connectClient(session: WebSocketSession, id: String): String {
        clients[id] = session
        return id
    }

    fun disconnectClient(clientId: String) {
        clients.remove(clientId)
    }

    suspend fun saveMessage(senderId: String, bytes: ByteArray) {
        receivedBytes.add(bytes)
        sendToOtherClients(senderId, bytes)
    }

    private suspend fun sendToOtherClients(senderId: String, bytes: ByteArray) {
        clients.filterKeys { it != senderId }.forEach { (id, session) ->
            try {
                session.send(Frame.Binary(true, bytes))
            } catch (e: Exception) {
                logger.warn("Failed to send to $id: ${e.message}")
            }
        }
    }

    suspend fun saveToFile() {
        logger.info("Saving file.....")
        val file = generateTempFile()
        withContext(Dispatchers.IO) {
            BufferedOutputStream(FileOutputStream(file)).use { bos ->
                receivedBytes.forEach { bos.write(it) }
            }
            logger.info("Saved file: ${file.absoluteFile}")
        }
        receivedBytes.clear()
    }

    private fun generateTempFile(): File {
        val file = File("WHISPER_SERVER_test.pcm")
        if (file.exists()) file.delete()
        return file // ⚠️ raw PCM unless you're writing WAV headers
    }
}
