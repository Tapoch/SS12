package ru.alex.ss12.request

data class Request(val type: Type, val data: String) {
    enum class Type {
        INIT, MOVE
    }
}
