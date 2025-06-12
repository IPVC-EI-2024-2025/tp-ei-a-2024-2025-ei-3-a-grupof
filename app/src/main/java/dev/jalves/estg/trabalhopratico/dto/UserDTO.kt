package dev.jalves.estg.trabalhopratico.dto

import dev.jalves.estg.trabalhopratico.objects.Role
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateUserDTO(
    @SerialName("display_name")
    val displayName: String,
    val email: String,
    val username: String,
    val password: String,
    val role: Role = Role.EMPLOYEE
)

@Serializable
data class UpdateUserDTO(
    val id: String,
    @SerialName("display_name")
    val displayName: String? = null,
    val email: String? = null,
    val username: String? = null,
    val password: String? = null,
    @SerialName("profile_picture")
    val profilePicture: String? = null,
    val role: Role? = null
)
