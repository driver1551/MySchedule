package com.example.projectnailsschedule.domain.usecase.account

import com.example.projectnailsschedule.domain.repository.api.LogoutApi
import com.example.projectnailsschedule.utils.Util
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LogoutUseCase {

    suspend fun execute(jwt: String): Boolean {
        val baseUrl = getBaseUrl()

        return try {
            val client = createOkHttpClient()
            val retrofit = createRetrofit(baseUrl, client)

            val logoutApi = retrofit.create(LogoutApi::class.java)

            // Выполняем запрос на выход
            val response = executeLogoutRequest(logoutApi, jwt)

            response.isSuccessful
        } catch (e: Exception) {
            // Логируем и возвращаем false при ошибке
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

    private suspend fun executeLogoutRequest(logoutApi: LogoutApi, jwt: String): Response<Unit> {
        return logoutApi.logout(jwt)
    }

}
