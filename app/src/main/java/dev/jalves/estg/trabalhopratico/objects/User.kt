package dev.jalves.estg.trabalhopratico.objects

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User (
    val id: String = "",
    var email: String = "",
    @SerialName("display_name")
    val displayName: String = "",
    val username: String = "",
    val role: Role = Role.EMPLOYEE
)

@Serializable
enum class Role(
    val value: String,
    val description: String,
    val color: Color
) {
    @SerialName("Admin")
    ADMIN("Admin", "Administrator", Color.hsl(40f, 0.75f, 0.75f)),

    @SerialName("Manager")
    MANAGER("Manager", "Manager", Color.hsl(280f, 0.75f, 0.75f)),

    @SerialName("Employee")
    EMPLOYEE("Employee", "Employee", Color.hsl(119f, 0.75f, 0.75f));

    companion object {
        fun fromString(value: String): Role? {
            return entries.find { it.value.equals(value, ignoreCase = true) }
        }
    }
}