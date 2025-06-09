package dev.jalves.estg.trabalhopratico.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateUserDTO(
    @SerialName("display_name")
    val displayName: String,
    val email: String,
    val username: String,
    val password: String
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
    val profilePicture: String? = null
)
