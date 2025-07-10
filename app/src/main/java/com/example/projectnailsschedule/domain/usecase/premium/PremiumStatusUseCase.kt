package com.example.projectnailsschedule.domain.usecase.premium

import com.example.projectnailsschedule.domain.models.dto.PremiumStatusDto
import com.example.projectnailsschedule.domain.repository.api.premiumApi.PremiumApi
import com.example.projectnailsschedule.utils.Util
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class SetPremiumStatusUseCase {
    suspend operator fun invoke(token: String, isPremium: Boolean) {
        val baseUrl = Util().getBaseUrl()
        val client = createOkHttpClient()
        val retrofit = createRetrofit(baseUrl, client)
        val premiumApi = retrofit.create(PremiumApi::class.java)

        val dto = PremiumStatusDto(isPremium)

        val response = premiumApi.postPremiumStatus(dto, token)

        if (!response.isSuccessful) {
            throw Exception("Ошибка при установке премиум статуса: ${response.code()}")
        }
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
