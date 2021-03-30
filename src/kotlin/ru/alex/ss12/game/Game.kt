package ru.alex.ss12.game

import io.ktor.http.cio.websocket.DefaultWebSocketSession
import io.ktor.http.cio.websocket.send

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import org.slf4j.LoggerFactory

import ru.alex.ss12.db.LocalStorage
import ru.alex.ss12.game.model.MoveDirection
import ru.alex.ss12.game.model.World
import ru.alex.ss12.model.User
import ru.alex.ss12.request.data.InitData
import ru.alex.ss12.request.data.MoveData
import ru.alex.ss12.response.CoolDownResponse
import ru.alex.ss12.response.WorldResponse
import ru.alex.ss12.response.ItemsResponse
import ru.alex.ss12.response.PlayersResponse
import ru.alex.ss12.response.Response

class Game(private val world: World) {

    private val logger = LoggerFactory.getLogger(Game::class.java)

    private val connections = mutableMapOf<DefaultWebSocketSession, User>()

    init {
        CoolDownTimerTask {
            GlobalScope.launch {
                sendAll(CoolDownResponse(), needLog = false)
            }
        }.start()
    }

    suspend fun actionConnect(webSocket: DefaultWebSocketSession, initData: InitData) {
        val user = LocalStorage.getUser(initData.username)
        connections[webSocket] = user

        send(webSocket, WorldResponse(world.map))
        send(webSocket, PlayersResponse(connections.values.toTypedArray()))
        send(webSocket, ItemsResponse(world.items.values))

        logger.debug("Action connect")
    }

    suspend fun actionDisconnect(webSocket: DefaultWebSocketSession) {
        connections.remove(webSocket)

        sendAll(PlayersResponse(connections.values.toTypedArray()))

        logger.debug("Action disconnect")
    }

    suspend fun actionMove(webSocket: DefaultWebSocketSession, moveData: MoveData) {
        val user = connections[webSocket]
        if (user == null) {
            logger.error("User is not connected!")
            return
        }

        val moveDirection = MoveDirection.fromString(moveData.direction)
        val moveResult = world.moveUser(user, moveDirection)
        if (moveResult.isMoved) {
            sendAll(PlayersResponse(connections.values.toTypedArray()))
            logger.debug("Player ${user.name} moved to ${moveData.direction}")

            if (moveResult.isPickedUpItem) {
                sendAll(ItemsResponse(world.items.values))
            }
        } else {
            logger.debug("Incorrect move: ${moveData.direction}")
        }
    }

    private suspend fun send(webSocket: DefaultWebSocketSession, response: Response, needLog: Boolean = true) {
        val text = response.toJson()
        if (needLog) {
            logger.debug("Send:\n$text")
        }
        webSocket.send(text)
    }

    private suspend fun sendAll(response: Response, needLog: Boolean = true) {
        val text = response.toJson()
        if (needLog) {
            logger.debug("Send all:\n$text")
        }
        connections.forEach { it.key.send(text) }
    }
}