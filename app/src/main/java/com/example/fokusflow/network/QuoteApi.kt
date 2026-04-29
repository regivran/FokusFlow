package com.example.fokusflow.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface QuoteApi {
    @GET("random")
    suspend fun getRandomQuote(): List<Quote>

    companion object {
        private const val BASE_URL = "https://zenquotes.io/api/"

        fun create(): QuoteApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(QuoteApi::class.java)
        }
    }
}
