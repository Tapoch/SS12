package ru.alex.ss12.game.tasks

import java.util.TimerTask


class CoolDownTimerTask(private val action:() -> Unit) : TimerTask() {

    companion object {
        const val COOL_DOWN_TIME_IN_MS = 400L
    }

    override fun run() {
        action.invoke()
    }
}
