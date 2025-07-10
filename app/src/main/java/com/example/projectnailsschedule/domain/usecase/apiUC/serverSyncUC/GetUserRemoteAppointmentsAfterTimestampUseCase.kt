package com.example.projectnailsschedule.domain.usecase.apiUC.serverSyncUC

import android.util.Log
import com.example.projectnailsschedule.domain.models.AppointmentModelDb
import com.example.projectnailsschedule.domain.repository.api.userDataApi.AppointmentsApi
import com.example.projectnailsschedule.utils.Util
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GetUserRemoteAppointmentsAfterTimestampUseCase {
    private val log = this::class.simpleName

    suspend fun execute(token: String, timestamp: Long?): List<AppointmentModelDb>? {
        val baseUrl = getBaseUrl()

        return try {
            val client = createOkHttpClient()
            val retrofit = createRetrofit(baseUrl, client)

            val appointmentsApi = retrofit.create(AppointmentsApi::class.java)

            val response = executeRequest(appointmentsApi, token, timestamp)

            return response
        } catch (e: Exception) {
            Log.e(log, e.message.toString())
            null
        }
    }

    private fun getBaseUrl(): String {
        return Util().getBaseUrl()
    }

    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    private fun createRetrofit(baseUrl: String, client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private suspend fun executeRequest(
        appointmentsApi: AppointmentsApi,
        token: String,
        timestamp: Long?
    ): List<AppointmentModelDb> {
        return appointmentsApi.getRemoteAppointmentsAfterTimestamp(token, timestamp)
    }
}