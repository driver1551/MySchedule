package com.example.projectnailsschedule.domain.usecase.apiUC.serverSyncUC.serverCalendarColorApiUC

import android.util.Log
import com.example.projectnailsschedule.domain.models.CalendarDateModelDb
import com.example.projectnailsschedule.domain.repository.api.calendarColorApi.CalendarColorApi
import com.example.projectnailsschedule.utils.Util

class GetAfterTimestampCalendarDateUseCase {
    private val log = this::class.simpleName
    private val baseUrl = Util().getBaseUrl()
    private val okHttpClient = Util().createOkHttpClient()
    private val retrofit = Util().createRetrofit(baseUrl, okHttpClient)

    suspend fun execute(token: String, timestamp: Long?): List<CalendarDateModelDb>? {
        return try {
            val calendarColorApi = retrofit.create(CalendarColorApi::class.java)

            val response = executeRequest(calendarColorApi, token, timestamp)

            return response
        } catch (e: Exception) {
            Log.e(log, e.message.toString())
            null
        }
    }

    private suspend fun executeRequest(
        calendarColorApi: CalendarColorApi,
        token: String,
        timestamp: Long?
    ): List<CalendarDateModelDb> {
        return calendarColorApi.getDataAfterTimestamp(token, timestamp)
    }
}