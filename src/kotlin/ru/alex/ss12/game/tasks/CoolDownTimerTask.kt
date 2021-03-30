package ru.alex.ss12.game.tasks

import org.slf4j.LoggerFactory
import java.lang.Exception
import java.util.Timer
import java.util.TimerTask


class CoolDownTimerTask(private val action:() -> Unit) : TimerTask() {

    companion object {
        private const val COOL_DOWN_TIME_IN_MS = 400L
        private val COOL_DOWN_TIMER = Timer("Cooldown_timer")
    }

    private val logger = LoggerFactory.getLogger(CoolDownTimerTask::class.java)

    override fun run() {
        action.invoke()
    }

    fun start() {
        try {
            COOL_DOWN_TIMER.schedule(this, 0,
                COOL_DOWN_TIME_IN_MS
            )
        } catch (e: IllegalStateException) {
            logger.error("Timer cancelled!")
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}