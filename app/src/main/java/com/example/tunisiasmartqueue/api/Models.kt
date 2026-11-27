package com.example.tunisiasmartqueue.api

// Authentication
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

// Profile
data class ProfileUpdateRequest(
    val firstName: String,
    val lastName: String,
    val phone: String?
)

// Tickets
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


data class ServiceResponse(
    val id: Long,
    val name: String,
    val letter: String,
    val estimatedTime: Int,
    val establishmentType: String
)

data class AgencyResponse(
    val id: Long,
    val name: String,
    val type: String,
    val address: String,
    val lat: Double,
    val lng: Double
)