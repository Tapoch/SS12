package ru.alex.ss12.response

import ru.alex.ss12.model.User

data class PlayersResponse(val type: String, val players: Array<User>) : Response
