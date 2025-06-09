package dev.jalves.estg.trabalhopratico.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class CreatePhotoDTO(
    @SerialName("uploaded_at")
    val uploadedAt: String,
    @SerialName("photo_url")
    val photoUrl: String,
    @SerialName("log_id")
    val logId: String
)