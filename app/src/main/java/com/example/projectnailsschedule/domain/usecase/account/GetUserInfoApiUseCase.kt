package com.example.projectnailsschedule.domain.usecase.account

import android.util.Log
import com.example.projectnailsschedule.domain.models.dto.UserInfoDto
import com.example.projectnailsschedule.domain.models.dto.UsernameRequestDto
import com.example.projectnailsschedule.domain.repository.api.GetUserDataApi
import com.example.projectnailsschedule.utils.Util
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GetUserInfoApiUseCase {
    private val log = this::class.simpleName

    suspend fun execute(username: String, jwt: String): Response<UserInfoDto>? {
        val baseUrl = getBaseUrl()

        return try {
            val client = createOkHttpClient()
            val retrofit = createRetrofit(baseUrl, client)

            val getUserDataApi = retrofit.create(GetUserDataApi::class.java)

            return executeRequest(getUserDataApi, username, jwt)
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

    private suspend fun executeRequest(
        getUserDataApi: GetUserDataApi,
        username: String,
        jwt: String
    ): Response<UserInfoDto> {
        return getUserDataApi.getUserData(usernameRequest = UsernameRequestDto(username), jwt)
    }
}