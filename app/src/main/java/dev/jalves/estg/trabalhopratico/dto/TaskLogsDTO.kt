package dev.jalves.estg.trabalhopratico.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskLogDTO(


    val date: String,

    val location: String,

    val completionRate: Float,

    val timeSpent: Float,

    val roles: String
)
