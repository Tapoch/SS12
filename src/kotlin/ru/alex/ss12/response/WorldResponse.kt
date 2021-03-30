package ru.alex.ss12.response

data class WorldResponse(val world: Array<IntArray>) : Response(Type.WORLD)
