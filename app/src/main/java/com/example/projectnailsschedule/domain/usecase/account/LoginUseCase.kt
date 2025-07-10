package com.example.projectnailsschedule.domain.usecase.account

import android.util.Log
import com.example.projectnailsschedule.domain.models.dto.LoginResponseDto
import com.example.projectnailsschedule.domain.models.User
import com.example.projectnailsschedule.domain.repository.api.LoginApi
import com.example.projectnailsschedule.utils.Util
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginUseCase {
    private val log = this::class.simpleName

    suspend fun execute(user: User): Response<LoginResponseDto>? {
        val baseUrl = getBaseUrl()

        return try {
            val client = createOkHttpClient()
            val retrofit = createRetrofit(baseUrl, client)

            val loginApi = retrofit.create(LoginApi::class.java)

            return executeLoginRequest(loginApi, user)

        } catch (e: Exception) {
            Log.e(log, e.toString())
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

    private suspend fun executeLoginRequest(loginApi: LoginApi, user: User): Response<LoginResponseDto> {
        return loginApi.loginUser(user)
    }
}