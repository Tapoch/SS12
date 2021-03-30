package ru.alex.ss12.response

import com.google.gson.GsonBuilder

interface Response {
    fun toJson(): String = GsonBuilder().create().toJson(this)
}