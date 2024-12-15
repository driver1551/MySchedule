package com.example.projectnailsschedule.domain.usecase.apiUC.serverSyncUC.serverCalendarColorApiUC

import android.util.Log
import com.example.projectnailsschedule.domain.models.CalendarDateModelDb
import com.example.projectnailsschedule.domain.repository.api.calendarColorApi.CalendarColorApi
import com.example.projectnailsschedule.util.Util
import retrofit2.Response

class DeleteRemoteCalendarDateUseCase {
    private val log = this::class.simpleName
    private val baseUrl = Util().getBaseUrl()
    private val okHttpClient = Util().createOkHttpClient()
    private val retrofit = Util().createRetrofit(baseUrl, okHttpClient)

    suspend fun execute(calendarDateModelDb: CalendarDateModelDb, jwt: String): String {
        return try {
            val calendarColorApi = retrofit.create(CalendarColorApi::class.java)

            val response = executeRequest(calendarColorApi, calendarDateModelDb, jwt)

            return response.code().toString()
        } catch (e: Exception) {
            Log.e(log, e.message.toString())
            "Возникла непредвиденная ошибка"
        }
    }

    private suspend fun executeRequest(
        calendarColorApi: CalendarColorApi,
        calendarDateModelDb: CalendarDateModelDb,
        jwt: String
    ): Response<Unit> {
        return calendarColorApi.delete(calendarDateModelDb, jwt)
    }
}