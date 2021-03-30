package ru.alex.ss12.game.model

abstract class Item(val type: Type) {

    abstract val x: Int
    abstract val y: Int

    enum class Type {
        LAMP
    }
}
