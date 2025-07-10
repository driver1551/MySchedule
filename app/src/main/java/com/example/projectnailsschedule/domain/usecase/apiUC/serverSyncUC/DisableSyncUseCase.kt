package com.example.projectnailsschedule.domain.usecase.apiUC.serverSyncUC

import com.example.projectnailsschedule.domain.models.dto.UserInfoDto
import com.example.projectnailsschedule.domain.repository.api.userDataApi.AppointmentsApi
import com.example.projectnailsschedule.utils.Util
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DisableSyncUseCase {
    private val log = this::class.simpleName

    suspend fun execute(user: UserInfoDto, token: String): Boolean {
        val baseUrl = getBaseUrl()

        return try {
            val client = createOkHttpClient()
            val retrofit = createRetrofit(baseUrl, client)

            val appointmentsApi = retrofit.create(AppointmentsApi::class.java)

            val response = executeRequest(appointmentsApi, user, token)

            return response
        } catch (e: Exception) {
            false
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
        user: UserInfoDto,
        token: String
    ): Boolean {
        return appointmentsApi.disableSync(user, token)
    }
}