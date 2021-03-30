package ru.alex.ss12

import com.google.gson.GsonBuilder

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.pingPeriod
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.timeout
import io.ktor.routing.routing
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket

import kotlinx.coroutines.channels.ClosedReceiveChannelException

import org.slf4j.LoggerFactory

import ru.alex.ss12.game.Game
import ru.alex.ss12.game.model.World
import ru.alex.ss12.request.Request
import ru.alex.ss12.request.data.InitData
import ru.alex.ss12.request.data.MoveData

import java.time.Duration


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    install(ContentNegotiation) {
        gson {
        }
    }

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    val logger = LoggerFactory.getLogger(Application::class.java)
    val gson = GsonBuilder().create()
    val game = Game(world = World())

    routing {
        webSocket("/") {

            logger.debug("Match route: \'/\'")

            try {
                while (true) {
                    val frame = incoming.receive()
                    if (frame is Frame.Text) {
                        val text = frame.readText()

                        logger.debug("Received text:\n$text")

                        val request = gson.fromJson(text, Request::class.java)
                        when (request.type) {
                            Request.Type.INIT -> {
                                val initData = gson.fromJson(request.data, InitData::class.java)
                                game.actionConnect(this, initData)
                            }
                            Request.Type.MOVE -> {
                                val moveData = gson.fromJson(request.data, MoveData::class.java)
                                game.actionMove(this, moveData)
                            }
                        }
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                // Do nothing!
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                game.actionDisconnect(this)
            }
        }
    }
}
