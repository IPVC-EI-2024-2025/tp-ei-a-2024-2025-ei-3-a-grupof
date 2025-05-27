package dev.jalves.estg.trabalhopratico.dto
import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskDTO(
    val name: String,
    val description: String,
    val status: String,

)
