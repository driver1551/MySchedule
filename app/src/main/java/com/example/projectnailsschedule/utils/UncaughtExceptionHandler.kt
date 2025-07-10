package com.example.projectnailsschedule.utils

import android.util.Log
import com.example.projectnailsschedule.domain.models.UserData
import com.example.projectnailsschedule.domain.models.UserDataManager
import com.example.projectnailsschedule.domain.usecase.apiUC.SendUserDataUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess

class UncaughtExceptionHandler(
    private val sendUserDataUseCase: SendUserDataUseCase
) : Thread.UncaughtExceptionHandler {

    @Volatile
    private var hasHandledException = false
    private val log = this::class.simpleName

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        if (hasHandledException) {
            // чтобы из разных потоков не вызывался - поставить флаг
            return
        }
        hasHandledException = true

        val userData = UserData(
            sessionId = UserDataManager.getUserData().sessionId,
            dateTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")),
            eventType = "Error",
            event = "Message ${throwable.message}. StackTrace: ${throwable.stackTrace[0]}"
        )



        CoroutineScope(Dispatchers.IO).launch {
            try {
                sendUserDataUseCase.execute()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // Выводим лог ошибки
                Log.e(log, Log.getStackTraceString(throwable))
                exitProcess(1)
            }
        }
    }
}
