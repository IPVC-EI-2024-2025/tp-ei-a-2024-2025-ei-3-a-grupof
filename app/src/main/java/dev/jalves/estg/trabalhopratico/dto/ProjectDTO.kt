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

@Serializable
data class UserDTO (
    val id: String,
    @SerialName("display_name")
    val displayName: String
)

@Serializable
data class ProjectDTO (
    val id: String = "",
    val name: String = "",
    val description: String = "",
    @SerialName("start_date")
    val startDate: String = "",
    @SerialName("due_date")
    val dueDate: String = "",
    val status: String = "",
    val manager: UserDTO?,
    val employees: List<UserDTO>
)