package dev.jalves.estg.trabalhopratico.dto

import dev.jalves.estg.trabalhopratico.objects.Role
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateProjectDTO (
    val name: String,
    val description: String,
    @SerialName("start_date")
    val startDate: String,
    @SerialName("due_date")
    val dueDate: String,
    val managerID: String? = null
)

@Serializable
data class UpdateProjectDTO (
    val id: String,
    val name: String? = null,
    val description: String? = null,
    val startDate: String? = null,
    val dueDate: String? = null,
    val status: String? = null,
    val managerID: String? = null,
)

@Serializable
data class UserDTO (
    val id: String,
    @SerialName("display_name")
    val displayName: String,
    val username: String,
    val role: Role
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