package com.example.tunisiasmartqueue.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL = "http://127.0.0.1:8081/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val profilApi: ProfilApi by lazy {
        retrofit.create(ProfilApi::class.java)
    }

    val ticketsApi: TicketsApi by lazy {
        retrofit.create(TicketsApi::class.java)
    }
}
