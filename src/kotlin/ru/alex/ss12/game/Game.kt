package ru.alex.ss12.game

import io.ktor.http.cio.websocket.DefaultWebSocketSession
import io.ktor.http.cio.websocket.send

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import org.slf4j.LoggerFactory

import ru.alex.ss12.db.LocalStorage
import ru.alex.ss12.game.model.Item
import ru.alex.ss12.game.model.MoveDirection
import ru.alex.ss12.game.model.World
import ru.alex.ss12.game.tasks.TaskController
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
    private val taskController = TaskController()

    private var isPlayerChanged = false
    private var isItemsChanged = false

    init {
        taskController.startCoolDownTask {
            GlobalScope.launch {
                connections.values.forEach { it.enableMove() }
                updateWorldToUsers()
            }
        }
    }

    suspend fun actionConnect(webSocket: DefaultWebSocketSession, initData: InitData) {
        val user = LocalStorage.getUser(initData.username)
        connections[webSocket] = user

        send(webSocket, WorldResponse(world.map))
        send(webSocket, PlayersResponse(connections.values.toTypedArray()))
        send(webSocket, ItemsResponse(world.items.values))
        isPlayerChanged = true

        logger.debug("Action connect")
    }

    suspend fun actionDisconnect(webSocket: DefaultWebSocketSession) {
        connections.remove(webSocket)

        isPlayerChanged = true

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
            send(webSocket, PlayersResponse(connections.values.toTypedArray()))
            isPlayerChanged = true
            logger.debug("Player ${user.name} moved to ${moveData.direction}")

            if (moveResult.isPickedUpItem) {
                if (user.items.contains(Item.Type.LAMP)) {
                    taskController.startLampTimerTask(user) { userLampRemove ->
                        GlobalScope.launch {
                            userLampRemove.removeItem(Item.Type.LAMP)
                        }
                    }
                }
                isItemsChanged = true
                send(webSocket, ItemsResponse(world.items.values))
            }
        } else {
            logger.debug("Incorrect move: ${moveData.direction}")
        }
    }

    private suspend fun updateWorldToUsers() {
        sendAll(CoolDownResponse(), needLog = false)
        if (isPlayerChanged) {
            sendAll(PlayersResponse(connections.values.toTypedArray()))
            isPlayerChanged = false
        }
        if (isItemsChanged) {
            sendAll(ItemsResponse(world.items.values))
            isItemsChanged = false
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