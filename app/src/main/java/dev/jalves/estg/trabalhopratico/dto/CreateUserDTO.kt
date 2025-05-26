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
