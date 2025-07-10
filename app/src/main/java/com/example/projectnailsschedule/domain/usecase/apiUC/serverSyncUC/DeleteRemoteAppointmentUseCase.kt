package com.example.projectnailsschedule.domain.usecase.apiUC.serverSyncUC

import com.example.projectnailsschedule.domain.models.AppointmentModelDb
import com.example.projectnailsschedule.domain.repository.api.userDataApi.AppointmentsApi
import com.example.projectnailsschedule.utils.Util
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DeleteRemoteAppointmentUseCase {
    private val log = this::class.simpleName

    suspend fun execute(appointment: AppointmentModelDb, jwt: String): String {
        val baseUrl = getBaseUrl()

        return try {
            val client = createOkHttpClient()
            val retrofit = createRetrofit(baseUrl, client)

            val appointmentsApi = retrofit.create(AppointmentsApi::class.java)

            // Выполняем запрос на авторизацию
            val response = executeRequest(appointmentsApi, appointment, jwt)
            delay(100L)
            return response.code().toString()
        } catch (e: Exception) {
            "Возникла непредвиденная ошибка"
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
        appointment: AppointmentModelDb,
        jwt: String
    ): Response<Unit> {
        return appointmentsApi.deleteAppointment(appointment, jwt)
    }
}