package com.example.projectnailsschedule.domain.models

import android.os.Build
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.projectnailsschedule.BuildConfig
import com.example.projectnailsschedule.utils.Util
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.LinkedList

data class UserData(
    var sessionId: String = Util().generateUniqueId(),
    var model: String = Build.MODEL,
    var device: String = Build.DEVICE,
    var dateTime: String = "",
    var appVersionName: String = BuildConfig.VERSION_NAME,
    var eventType: String = "",
    var event: String = ""
)

// Ключевое слово Object используется для создания singleton
object UserDataManager {
    private val log = this::class.simpleName

    private val userData = UserData()
    var userDateQueue = MutableLiveData(LinkedList<UserData>())

    fun getUserData(): UserData {
        return userData
    }

    @Synchronized
    fun updateUserData(eventType: String = "LifeCycle", event: String?) {
        val newUserData = userData.copy(
            dateTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")),
            eventType = eventType,
            event = event ?: userData.event
        )
        addUserDateQueue(newUserData)
    }

    private fun addUserDateQueue(userData: UserData) {
        val queue = userDateQueue.value ?: LinkedList()
        queue.add(userData)
        userDateQueue.postValue(queue)
        Log.i(log, "Добавлена в очередь $userData")
    }

    fun pollUserDateQueue(): UserData? {
        val queue = userDateQueue.value ?: LinkedList()
        return if (queue.isNotEmpty()) {
            userDateQueue.postValue(queue)
            queue.poll()
        } else {
            null
        }
    }
}


