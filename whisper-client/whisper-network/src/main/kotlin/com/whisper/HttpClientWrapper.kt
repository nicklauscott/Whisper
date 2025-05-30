package com.whisper

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess

object HttpClientWrapper {

    private val client = HttpClient(CIO) {
        install(WebSockets) {
            pingIntervalMillis = 10_000
        }
    }

    private var url: String? = null
    private val id = ('A'..'z').random().toString() + (0..9).random()
    private val socketAddress: String?
        get() = if (url == null) null else "$url/voip/$id"

    private var scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val sendingByteChannel = Channel<ByteArray>(Channel.UNLIMITED)
    @JvmStatic
    val receivingQueue: BlockingQueue<ByteArray> = LinkedBlockingQueue()

    private val isStreaming = AtomicBoolean(false)

    @JvmStatic
    fun isStreaming(): Boolean = isStreaming.get()

    @JvmStatic
    fun stream(byte: ByteArray) {
        if (!isStreaming.get()) return
        // Use coroutine to send without blocking Java thread
        scope.launch {
            sendingByteChannel.send(byte)
        }
    }

    @JvmStatic
    fun startStreamingBlocking() {
        if (isStreaming.getAndSet(true)) return

        runBlocking {
            try {
                client.ws(socketAddress ?: "ws://localhost:8080/voip/$id") {
                    isStreaming.set(true)

                    val sendJob = launch {
                        for (bytes in sendingByteChannel) {
                            send(Frame.Binary(true, bytes))
                            delay(50)
                        }
                    }

                    val receiveJob = launch {
                        for (frame in incoming) {
                            when (frame) {
                                is Frame.Binary -> receivingQueue.add(frame.readBytes())
                                is Frame.Text -> println("HttpClientWrapper -> Received text: ${frame.readText()}")
                                else -> {} // Ignore
                            }

                        }
                    }

                    sendJob.join()
                    receiveJob.cancelAndJoin()
                }
            } catch (e: Exception) {
                println("HttpClientWrapper -> WebSocket error: ${e.message}")
                exitProcess(0)
            } finally {
                println("HttpClientWrapper -> WebSocket closed.")
                isStreaming.set(false)
                resetScope()
            }
        }
    }

    @JvmStatic
    fun stopSendingAndReceiving() {
        if (!isStreaming.getAndSet(false)) return
        scope.cancel()
        sendingByteChannel.close()
        receivingQueue.clear()
        resetScope()
    }

    @JvmStatic
    fun setUrl(url: String) {
        this.url = url.replace("https", "wss").dropLastWhile { it == '/' }
    }

    @JvmStatic
    fun getUrl(): String = url ?: "Url not set"

    private fun resetScope() {
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

}