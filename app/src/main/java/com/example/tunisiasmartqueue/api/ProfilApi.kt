
package com.example.tunisiasmartqueue.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT

interface ProfilApi {

    @GET("api/users/me")
    fun getProfil(
        @Header("Authorization") token: String
    ): Call<UserResponse>

    @PUT("api/users/me")
    fun updateProfil(
        @Header("Authorization") token: String,
        @Body request: ProfileUpdateRequest
    ): Call<UserResponse>
}
