package dev.jalves.estg.trabalhopratico.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Project (
    val id: String = "",
    val name: String = "",
    val description: String = "",
    @SerialName("start_date")
    val startDate: String = "",
    @SerialName("due_date")
    val dueDate: String = "",
    val status: String = "",
    @SerialName("created_by_id")
    val createdByID: String = "",
    @SerialName("manager_id")
    val managerID: String? = null
)