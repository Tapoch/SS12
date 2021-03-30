package ru.alex.ss12.response

enum class ResponseType(val typeName: String) {
    WORLD("world"),
    COOLDOWN("cooldown"),
    PLAYERS("players")
}