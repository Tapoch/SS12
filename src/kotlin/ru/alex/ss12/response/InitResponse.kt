package ru.alex.ss12.response

data class InitResponse(val world: Array<IntArray>) : Response(Type.WORLD)
