package ru.alex.ss12.game.tasks

import ru.alex.ss12.model.User
import java.util.*

class LampTimerTask(private val user: User, private val action: (User) -> Unit) : TimerTask() {

    companion object {
        const val LAMP_TIMER_MS = 5000L
    }

    override fun run() {
        action.invoke(user)
    }
}
