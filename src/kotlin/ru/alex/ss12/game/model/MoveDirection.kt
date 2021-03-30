package ru.alex.ss12.game.model

enum class MoveDirection {
    UP, DOWN, LEFT, RIGHT, UNKNOWN;

    companion object {
        fun fromString(direction: String): MoveDirection {
            return when (direction) {
                "up" -> {
                    UP
                }
                "down" -> {
                    DOWN
                }
                "left" -> {
                    LEFT
                }
                "right" -> {
                    RIGHT
                }
                else -> {
                    UNKNOWN
                }
            }
        }
    }
}