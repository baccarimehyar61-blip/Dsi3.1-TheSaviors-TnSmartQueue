package com.example.tunisiasmartqueue.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CatalogApi {

    @GET("api/services")
    fun getServices(): Call<List<ServiceResponse>>

    @GET("api/agencies")
    fun getAgencies(
        @Query("category") category: String
    ): Call<List<AgencyResponse>>
}