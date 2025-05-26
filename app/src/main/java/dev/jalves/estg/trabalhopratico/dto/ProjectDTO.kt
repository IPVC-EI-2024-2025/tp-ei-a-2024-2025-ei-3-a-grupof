package dev.jalves.estg.trabalhopratico.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateProjectDTO (
    val name: String,
    val description: String,
    @SerialName("start_date")
    val startDate: String,
    @SerialName("due_date")
    val dueDate: String
)