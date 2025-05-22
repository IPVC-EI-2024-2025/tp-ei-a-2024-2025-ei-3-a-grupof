package dev.jalves.estg.trabalhopratico.objects

data class Project (
    val id: String,
    val name: String,
    val description: String,
    val startDate: String,
    val dueDate: String,
    val status: String,
    val createdByID: String,
    val managerID: String?
)