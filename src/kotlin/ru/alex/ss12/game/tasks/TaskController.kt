package ru.alex.ss12.game.tasks

import org.slf4j.LoggerFactory
import ru.alex.ss12.model.User
import java.lang.Exception
import java.util.*

class TaskController {
    private val logger = LoggerFactory.getLogger(TaskController::class.java)
    private val timer = Timer("Task-controller-timer")

    private var isCoolDownTaskStarted = false
    private val lampTimerScheduledTasksForUsers = mutableListOf<User>()

    fun startCoolDownTask(action:() -> Unit) {
        if (isCoolDownTaskStarted) {
            return
        }

        isCoolDownTaskStarted = try {
            timer.schedule(CoolDownTimerTask(action), 0, CoolDownTimerTask.COOL_DOWN_TIME_IN_MS)
            true
        } catch (e: IllegalStateException) {
            logger.error("Timer cancelled!")
            e.printStackTrace()
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun startLampTimerTask(user: User, action: (User) -> Unit) {
        if (lampTimerScheduledTasksForUsers.contains(user)) {
            return
        }

        try {
            timer.schedule(LampTimerTask(user) {
                lampTimerScheduledTasksForUsers.remove(user)
                action.invoke(it)
            }, LampTimerTask.LAMP_TIMER_MS)
        } catch (e: IllegalStateException) {
            logger.error("Timer cancelled!")
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}