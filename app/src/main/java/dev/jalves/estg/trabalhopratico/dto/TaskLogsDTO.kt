package dev.jalves.estg.trabalhopratico.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskLogDTO(
    @SerialName("user_id")
    val userId: String,
    @SerialName("task_id")
    val taskId: String,
    val date: String,
    val location: String,
    @SerialName("completion_rate")
    val completionRate: Float,
    @SerialName("time_spent")
    val timeSpent: Float,
    val notes: String? = null
)