package ru.alex.ss12.db

import ru.alex.ss12.model.User

object LocalStorage {
    private val users = mutableMapOf<String, User>()

    fun getUser(username: String): User {
        return users[username] ?: return addUser(username)
    }

    private fun addUser(username: String): User = User(username).apply {
        users[username] = this
    }
}