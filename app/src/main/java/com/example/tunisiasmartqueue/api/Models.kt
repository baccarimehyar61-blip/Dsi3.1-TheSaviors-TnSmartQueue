
package com.example.tunisiasmartqueue.api

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String?,
    val password: String
)

data class UserResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String?,
    val role: String,
    val createdAt: String?
)

data class AuthResponse(
    val token: String,
    val tokenType: String,
    val expiresIn: Long,
    val user: UserResponse
)

data class ProfileUpdateRequest(
    val firstName: String,
    val lastName: String,
    val phone: String?
)
