package dev.jalves.estg.trabalhopratico.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateUserDTO(
    @SerialName("display_name")
    val name: String,
    val email: String,
    val username: String,
    val password: String
)

data class UpdateUserDTO(
    val id: String,
    val name: String? = null,
    val email: String? = null,
    val username: String? = null,
    val password: String? = null,
    val profilePicture: String? = null
)