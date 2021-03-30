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
import ru.alex.ss12.response.InitResponse
import ru.alex.ss12.response.PlayersResponse
import ru.alex.ss12.response.ResponseType

class Game(private val world: World) {

    private val logger = LoggerFactory.getLogger(Game::class.java)

    private val connections = mutableMapOf<DefaultWebSocketSession, User>()

    init {
        CoolDownTimerTask {
            GlobalScope.launch {
                sendAll(CoolDownResponse(ResponseType.COOLDOWN.typeName).toJson())
            }
        }.start()
    }

    suspend fun actionConnect(webSocket: DefaultWebSocketSession, initData: InitData) {
        val user = LocalStorage.getUser(initData.username)
        connections[webSocket] = user

        val response = InitResponse(ResponseType.WORLD.typeName, world.map).toJson()

        send(webSocket, response)

        logger.debug("Action connect")
    }

    suspend fun actionDisconnect(webSocket: DefaultWebSocketSession) {
        connections.remove(webSocket)

        val response = PlayersResponse(ResponseType.PLAYERS.typeName, connections.values.toTypedArray()).toJson()
        sendAll(response)

        logger.debug("Action disconnect")
    }

    suspend fun actionMove(webSocket: DefaultWebSocketSession, moveData: MoveData) {
        val user = connections[webSocket]
        if (user == null) {
            logger.error("User is not connected!")
            return
        }

        val moveDirection = MoveDirection.fromString(moveData.direction)
        if (world.isCanMove(user, moveDirection)) {
            user.move(moveDirection)
            val response = PlayersResponse(ResponseType.PLAYERS.typeName, connections.values.toTypedArray()).toJson()
            sendAll(response)
            logger.debug("Player ${user.name} moved to ${moveData.direction}")
        } else {
            logger.debug("Incorrect move: ${moveData.direction}")
        }
    }

    private suspend fun send(webSocket: DefaultWebSocketSession, text: String) {
        logger.debug("Send:\n$text")
        webSocket.send(text)
    }

    private suspend fun sendAll(text: String) {
        logger.debug("Send all:\n$text")
        connections.forEach { it.key.send(text) }
    }
}