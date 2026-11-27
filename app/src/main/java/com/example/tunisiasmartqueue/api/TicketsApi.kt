package com.example.tunisiasmartqueue.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface TicketsApi {

    @GET("api/tickets/me")
    fun getTickets(
        @Header("Authorization") token: String
    ): Call<List<TicketResponse>>

    @POST("api/tickets")
    fun createTicket(
        @Header("Authorization") token: String,
        @Body request: CreateTicketRequest
    ): Call<TicketResponse>
}