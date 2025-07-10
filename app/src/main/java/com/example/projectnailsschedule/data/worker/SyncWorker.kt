package com.example.projectnailsschedule.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.projectnailsschedule.domain.models.AppointmentModelDb
import com.example.projectnailsschedule.domain.models.CalendarDateModelDb
import com.example.projectnailsschedule.domain.models.dto.UserInfoDto
import com.example.projectnailsschedule.domain.models.dto.UserInfoDtoManager
import com.example.projectnailsschedule.domain.usecase.account.GetJwt
import com.example.projectnailsschedule.domain.usecase.account.GetUserInfoApiUseCase
import com.example.projectnailsschedule.domain.usecase.apiUC.localSyncDbUC.GetBySyncUuidUseCase
import com.example.projectnailsschedule.domain.usecase.apiUC.localSyncDbUC.GetDeletedAppointmentsUseCase
import com.example.projectnailsschedule.domain.usecase.apiUC.localSyncDbUC.GetNotSyncAppointmentsUseCase
import com.example.projectnailsschedule.domain.usecase.apiUC.serverSyncUC.DeleteRemoteAppointmentUseCase
import com.example.projectnailsschedule.domain.usecase.apiUC.serverSyncUC.GetLastRemoteAppointmentTimestamp
import com.example.projectnailsschedule.domain.usecase.apiUC.serverSyncUC.GetUserRemoteAppointmentsAfterTimestampUseCase
import com.example.projectnailsschedule.domain.usecase.apiUC.serverSyncUC.PostAppointmentUseCase
import com.example.projectnailsschedule.domain.usecase.apiUC.serverSyncUC.serverCalendarColorApiUC.DeleteRemoteCalendarDateUseCase
import com.example.projectnailsschedule.domain.usecase.apiUC.serverSyncUC.serverCalendarColorApiUC.GetAfterTimestampCalendarDateUseCase
import com.example.projectnailsschedule.domain.usecase.apiUC.serverSyncUC.serverCalendarColorApiUC.GetLastRemoteTimestampCalendarDateUseCase
import com.example.projectnailsschedule.domain.usecase.apiUC.serverSyncUC.serverCalendarColorApiUC.PostCalendarDateUseCase
import com.example.projectnailsschedule.domain.usecase.appointmentUC.DeleteAppointmentUseCase
import com.example.projectnailsschedule.domain.usecase.appointmentUC.GetOldUpdatedAppointmentsUseCase
import com.example.projectnailsschedule.domain.usecase.appointmentUC.InsertAppointmentUseCase
import com.example.projectnailsschedule.domain.usecase.appointmentUC.UpdateAppointmentUseCase
import com.example.projectnailsschedule.domain.usecase.calendarUC.DeleteCalendarDateUseCase
import com.example.projectnailsschedule.domain.usecase.calendarUC.GetBySyncUuidCalendarDateUseCase
import com.example.projectnailsschedule.domain.usecase.calendarUC.GetDeletedCalendarDateUseCase
import com.example.projectnailsschedule.domain.usecase.calendarUC.GetNotSyncCalendarDateUseCase
import com.example.projectnailsschedule.domain.usecase.calendarUC.GetOldUpdatedCalendarDateUseCase
import com.example.projectnailsschedule.domain.usecase.calendarUC.InsertCalendarDateUseCase
import com.example.projectnailsschedule.domain.usecase.calendarUC.SelectCalendarDateByDateUseCase
import com.example.projectnailsschedule.domain.usecase.calendarUC.UpdateCalendarDateUseCase
import com.example.projectnailsschedule.domain.usecase.premium.SetPremiumStatusUseCase
import com.example.projectnailsschedule.domain.usecase.rustore.CheckRuStoreLoginStatus
import com.example.projectnailsschedule.domain.usecase.rustore.GetPurchasesUseCase
import com.example.projectnailsschedule.domain.usecase.sharedPref.GetAppointmentLastUpdateUseCase
import com.example.projectnailsschedule.domain.usecase.sharedPref.GetCalendarDateLastUpdateUseCase
import com.example.projectnailsschedule.domain.usecase.sharedPref.SetAppointmentLastUpdateUseCase
import com.example.projectnailsschedule.domain.usecase.sharedPref.SetCalendarLastUpdateUseCase
import com.example.projectnailsschedule.utils.Util
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.rustore.sdk.billingclient.RuStoreBillingClient
import ru.rustore.sdk.billingclient.model.purchase.PurchaseAvailabilityResult
import ru.rustore.sdk.billingclient.utils.pub.checkPurchasesAvailability
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,

    // User
    private val getJwt: GetJwt,
    private val getUserInfoApi: GetUserInfoApiUseCase,

    // Appointments: DB
    private val insertAppointmentUseCase: InsertAppointmentUseCase,
    private val updateAppointmentUseCase: UpdateAppointmentUseCase,
    private val deleteAppointmentUseCase: DeleteAppointmentUseCase,
    private val getNotSyncAppointmentsUseCase: GetNotSyncAppointmentsUseCase,
    private val getDeletedAppointmentsUseCase: GetDeletedAppointmentsUseCase,
    private val getGetBySyncUuidUseCase: GetBySyncUuidUseCase,
    private val getOldUpdatedAppointmentsUseCase: GetOldUpdatedAppointmentsUseCase,

    // Appointments: API
    private val postAppointmentUseCase: PostAppointmentUseCase,
    private val deleteRemoteAppointmentUseCase: DeleteRemoteAppointmentUseCase,
    private val getUserLastRemoteAppointmentTimestamp: GetLastRemoteAppointmentTimestamp,
    private val getUserRemoteAppointmentsAfterTimestampUseCase: GetUserRemoteAppointmentsAfterTimestampUseCase,

    // CalendarDate: DB
    private val insertCalendarDateUseCase: InsertCalendarDateUseCase,
    private val deleteCalendarDateUseCase: DeleteCalendarDateUseCase,
    private val updateCalendarDateUseCase: UpdateCalendarDateUseCase,
    private val getNotSyncCalendarDateUseCase: GetNotSyncCalendarDateUseCase,
    private val getDeletedCalendarDateUseCase: GetDeletedCalendarDateUseCase,
    private val getBySyncUuidCalendarDateUseCase: GetBySyncUuidCalendarDateUseCase,
    private val selectCalendarDateByDateUseCase: SelectCalendarDateByDateUseCase,
    private val getOldUpdatedCalendarDateUseCase: GetOldUpdatedCalendarDateUseCase,

    // CalendarDate: API
    private val postRemoteCalendarDateUseCase: PostCalendarDateUseCase,
    private val deleteRemoteCalendarDateUseCase: DeleteRemoteCalendarDateUseCase,
    private val getLastRemoteTimestampCalendarDateUseCase: GetLastRemoteTimestampCalendarDateUseCase,
    private val getRemoteAfterTimestampCalendarDateUseCase: GetAfterTimestampCalendarDateUseCase,

    // SharedPrefs
    private val getAppointmentLastUpdateUseCase: GetAppointmentLastUpdateUseCase,
    private val setAppointmentLastUpdateUseCase: SetAppointmentLastUpdateUseCase,
    private val getCalendarDateLastUpdateUseCase: GetCalendarDateLastUpdateUseCase,
    private val setCalendarLastUpdateUseCase: SetCalendarLastUpdateUseCase,

    // Premium
    private val getPurchasesUseCase: GetPurchasesUseCase,
    private val checkRuStoreLoginStatus: CheckRuStoreLoginStatus,
    private val setPremiumStatusUseCase: SetPremiumStatusUseCase

) : CoroutineWorker(
    context, workerParams
) {
    override suspend fun doWork(): Result {
        return try {
            val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                Date()
            )
            Log.i("SyncWorker", "doWork запущен в $currentTime")

            val user = getUserInfoApi()
            val jwt = getJwt.execute()

            if (!checkSyncConditions(user, jwt)) {
                Log.i("SyncWorker", "checkSyncConditions вернул false, выход")
                return Result.success()
            }

            synchronizationAppointments(user!!, jwt!!)
            synchronizationCalendarDate(user, jwt)

            Log.i("SyncWorker", "Синхронизация выполнена успешно в $currentTime")
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Ошибка при выполнении синхронизации: ", e)
            Result.retry()
        }
    }

    suspend fun getUserInfoApi(): UserInfoDto? {
        val jwt = getJwt.execute() ?: return null
        val username = Util().extractUsernameFromJwt(jwt) ?: return null
        val userInfo = getUserInfoApi.execute(username, jwt)?.body() ?: return null

        UserInfoDtoManager.setUserDto(userInfo)
        return userInfo
    }

    private suspend fun checkSyncConditions(user: UserInfoDto?, jwt: String?): Boolean {
        if (user?.betaTester == true) return true

        if (!canSafelyCheckPurchases()) return false

        val isAuthorized = checkRuStoreLoginStatus.execute().await().authorized
        if (user == null || jwt == null || !isAuthorized) return false

        val hasPremium = getPurchasesUseCase.execute().fold(
            onSuccess = {
                val result = it.isNotEmpty()
                setPremiumStatusUseCase(jwt, result)
                result
            },
            onFailure = {
                setPremiumStatusUseCase(jwt ?: "", false)
                false
            }
        )

        return user.syncEnabled == true && hasPremium
    }

    private suspend fun canSafelyCheckPurchases(): Boolean = suspendCancellableCoroutine { cont ->
        RuStoreBillingClient.checkPurchasesAvailability()
            .addOnSuccessListener {
                cont.resume(it is PurchaseAvailabilityResult.Available) {}
            }
            .addOnFailureListener {
                cont.resume(false) {}
            }
    }

    private suspend fun synchronizationAppointments(user: UserInfoDto, jwt: String) {
        val lastRemoteUpdateTime = getAppointmentLastUpdateUseCase.execute()
        val lastRemoteTimestamp = getUserLastRemoteAppointmentTimestamp.execute(user, jwt) ?: 0L

        if (lastRemoteUpdateTime < lastRemoteTimestamp) {
            pullAppointments(lastRemoteUpdateTime, jwt)
        }

        val appointmentsForPull =
            (getNotSyncAppointmentsUseCase.execute() + getDeletedAppointmentsUseCase.execute()).sortedBy { it.syncTimestamp }
        if (appointmentsForPull.isNotEmpty()) {
            pushAppointments(user, jwt, appointmentsForPull)
        }
    }

    private suspend fun pushAppointments(
        user: UserInfoDto,
        jwt: String,
        appointments: List<AppointmentModelDb>
    ) {
        appointments.forEach { appointment ->
            when (appointment.syncStatus) {
                "NotSynchronized" -> {
                    if (appointment.userName == null) appointment.userName = user.username
                    val response = postAppointmentUseCase.execute(appointment, jwt)
                    if (response == "200") {
                        appointment.syncStatus = "Synchronized"
                        updateAppointmentUseCase.execute(appointment)
                    }
                }

                "DELETED" -> {
                    val response = deleteRemoteAppointmentUseCase.execute(appointment, jwt)
                    if (response == "200") deleteAppointmentUseCase.execute(appointment)
                }
            }
        }
    }

    private suspend fun pullAppointments(timestamp: Long, jwt: String) {
        val appointments = getUserRemoteAppointmentsAfterTimestampUseCase.execute(jwt, timestamp)
        appointments?.forEach { updatedAppointment ->
            val local = getGetBySyncUuidUseCase.execute(updatedAppointment.syncUUID!!)
            when {
                local == null && updatedAppointment.syncStatus == "NotSynchronized" -> {
                    insertAppointmentUseCase.execute(
                        updatedAppointment.copy(
                            _id = null,
                            syncStatus = "Synchronized"
                        )
                    )
                }

                local?.syncStatus == "DELETED" -> {
                    val result = deleteRemoteAppointmentUseCase.execute(updatedAppointment, jwt)
                    if (result == "200") deleteAppointmentUseCase.execute(local)
                }

                updatedAppointment.syncStatus == "NotSynchronized" && updatedAppointment.syncTimestamp!! > (local?.syncTimestamp
                    ?: 0) -> {
                    val updated = updatedAppointment.copy(
                        _id = local?._id,
                        clientId = local?.clientId,
                        syncStatus = "Synchronized"
                    )
                    updateAppointmentUseCase.execute(updated)
                }

                updatedAppointment.syncStatus == "DELETED" && local != null -> {
                    deleteAppointmentUseCase.execute(local)
                }
            }
            setAppointmentLastUpdateUseCase.execute(updatedAppointment.syncTimestamp!!)
        }
    }

    private suspend fun synchronizationCalendarDate(user: UserInfoDto, jwt: String) {
        val lastLocalTimestamp = getCalendarDateLastUpdateUseCase.execute()
        val lastRemoteTimestamp = getLastRemoteTimestampCalendarDateUseCase.execute(user, jwt) ?: 0L

        if (lastLocalTimestamp < lastRemoteTimestamp) {
            pullCalendarDate(lastLocalTimestamp, jwt)
        }

        val calendarDatesForPull =
            (getNotSyncCalendarDateUseCase.execute() + getDeletedCalendarDateUseCase.execute()).sortedBy { it.syncTimestamp }
        if (calendarDatesForPull.isNotEmpty()) {
            pushCalendarDate(user, jwt, calendarDatesForPull)
        }
    }

    private suspend fun pushCalendarDate(
        user: UserInfoDto,
        jwt: String,
        list: List<CalendarDateModelDb>
    ) {
        list.forEach { date ->
            when (date.syncStatus) {
                "NotSynchronized" -> {
                    if (date.userName == null) date.userName = user.username
                    val response = postRemoteCalendarDateUseCase.execute(date, jwt)
                    if (response == "200") {
                        date.syncStatus = "Synchronized"
                        updateCalendarDateUseCase.execute(date)
                    }
                }

                "DELETED" -> {
                    val response = deleteRemoteCalendarDateUseCase.execute(date, jwt)
                    if (response == "200") deleteCalendarDateUseCase.execute(date)
                }
            }
        }
    }

    private suspend fun pullCalendarDate(timestamp: Long, jwt: String) {
        val calendarDates = getRemoteAfterTimestampCalendarDateUseCase.execute(jwt, timestamp)
        calendarDates?.forEach { updated ->
            val local = getBySyncUuidCalendarDateUseCase.execute(updated.syncUUID!!)
                ?: selectCalendarDateByDateUseCase.execute(updated.date!!)

            if (local == null && updated.syncStatus == "NotSynchronized") {
                insertCalendarDateUseCase.execute(
                    updated.copy(
                        _id = null,
                        syncStatus = "Synchronized"
                    )
                )
            } else if (local.syncUUID != updated.syncUUID) {
                updateCalendarDateUseCase.execute(local!!.copy(syncUUID = updated.syncUUID))
            } else if (local.syncStatus == "DELETED") {
                val result = deleteRemoteCalendarDateUseCase.execute(updated, jwt)
                if (result == "200") deleteCalendarDateUseCase.execute(local)
            } else if (updated.syncStatus == "NotSynchronized" && updated.syncTimestamp!! > local.syncTimestamp!!) {
                updateCalendarDateUseCase.execute(
                    updated.copy(
                        _id = local._id,
                        syncStatus = "Synchronized"
                    )
                )
            } else if (updated.syncStatus == "DELETED") {
                deleteCalendarDateUseCase.execute(local)
            }
            setCalendarLastUpdateUseCase.execute(updated.syncTimestamp!!)
        }
    }
}