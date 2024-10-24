package com.example.projectnailsschedule.domain.usecase.account

import com.example.projectnailsschedule.BuildConfig
import com.example.projectnailsschedule.domain.models.dto.RegistrationRequestDto
import com.example.projectnailsschedule.domain.models.dto.RegistrationResponseDto
import com.example.projectnailsschedule.domain.repository.api.RegistrationApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RegistrationUseCase {
    private val log = this::class.simpleName

    suspend fun execute(username: String, email: String, password: String): String {
        val baseUrl = getBaseUrl()

        return try {
            val client = createOkHttpClient()
            val retrofit = createRetrofit(baseUrl, client)

            val registrationApi = retrofit.create(RegistrationApi::class.java)
            val registrationRequestDto = RegistrationRequestDto(username, email, password)

            // Выполняем запрос на авторизацию
            val response = executeRegistrationRequest(registrationApi, registrationRequestDto)

            return response.body()!!.status
        } catch (e: Exception) {
            "Возникла непредвиденная ошибка"
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

    private suspend fun executeRegistrationRequest(
        registrationApi: RegistrationApi,
        registrationRequestDto: RegistrationRequestDto
    ): Response<RegistrationResponseDto> {
        return registrationApi.registerUser(registrationRequestDto)
    }
}