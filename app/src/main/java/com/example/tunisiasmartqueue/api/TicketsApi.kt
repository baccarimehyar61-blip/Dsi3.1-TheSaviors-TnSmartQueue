
package com.example.tunisiasmartqueue.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

data class CreateTicketRequest(
    val serviceId: Long,
    val agencyId: Long
)

data class TicketResponse(
    val id: Long,
    val ticketNumber: String,
    val status: String,
    val createdAt: String,
    val serviceName: String,
    val serviceLetter: String,
    val estimatedTime: Int,
    val agencyName: String,
    val agencyAddress: String
)

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
