package dev.jalves.estg.trabalhopratico.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LogPhotos(
    val id: String,
    @SerialName("uploaded_at")
    val uploadedAt: String? = null,
    @SerialName("photo_url")
    val photoUrl: String,
    @SerialName("log_id")
    val logId: String
)