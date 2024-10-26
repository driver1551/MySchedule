package com.example.projectnailsschedule.domain.usecase.account

import android.util.Log
import com.example.projectnailsschedule.BuildConfig
import com.example.projectnailsschedule.domain.models.dto.RegistrationRequestDto
import com.example.projectnailsschedule.domain.models.dto.RegistrationResponseDto
import com.example.projectnailsschedule.domain.repository.api.ForgotPasswordApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SendAccConfirmation {
    private val log = this::class.simpleName

    suspend fun execute(usernameOrEmail: String): Response<RegistrationResponseDto>? {
        val baseUrl = getBaseUrl()

        return try {
            val client = createOkHttpClient()
            val retrofit = createRetrofit(baseUrl, client)

            val forgotPasswordApi = retrofit.create(ForgotPasswordApi::class.java)

            return request(
                forgotPasswordApi,
                RegistrationRequestDto(
                    username = usernameOrEmail,
                    email = usernameOrEmail,
                    password = ""
                )
            )

        } catch (e: Exception) {
            Log.e(log, e.toString())
            null
        }
    }

    private fun getBaseUrl(): String {
        return if (BuildConfig.DEBUG) {
            "http://10.0.2.2:8080/"
        } else {
            "https://myschedule.myddns.me"
        }
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

    private suspend fun request(
        forgotPasswordApi: ForgotPasswordApi,
        registrationRequestDto: RegistrationRequestDto
    ): Response<RegistrationResponseDto> {
        return forgotPasswordApi.forgotPassword(registrationRequestDto)
    }
}