package dev.jalves.estg.trabalhopratico.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaskLog(
    val id: String,
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
    val notes: String?,
    @SerialName("created_at")
    val createdAt: String
)