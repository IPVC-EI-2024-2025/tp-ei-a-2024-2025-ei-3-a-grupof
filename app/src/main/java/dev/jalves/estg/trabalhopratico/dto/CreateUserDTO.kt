package dev.jalves.estg.trabalhopratico.dto

data class CreateUserDTO(
    val name: String,
    val email: String,
    val username: String,
    val password: String
)
