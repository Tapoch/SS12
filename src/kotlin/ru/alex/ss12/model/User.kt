package ru.alex.ss12.model

import ru.alex.ss12.game.model.MoveDirection

data class User(val name: String, var x: Int = 1, var y: Int = 1) {
    fun move(direction: MoveDirection) {
        when (direction) {
            MoveDirection.UP -> y -= 1
            MoveDirection.DOWN -> y += 1
            MoveDirection.RIGHT -> x += 1
            MoveDirection.LEFT -> x -= 1
            MoveDirection.UNKNOWN -> { } //TODO
        }
    }
}
