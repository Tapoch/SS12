package ru.alex.ss12.response

import com.google.gson.GsonBuilder

abstract class Response(val type: Type) {
    fun toJson(): String = GsonBuilder().create().toJson(this)

    enum class Type {
        WORLD,
        COOLDOWN,
        PLAYERS,
        ITEMS
    }
}