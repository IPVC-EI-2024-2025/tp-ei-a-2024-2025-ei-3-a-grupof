package dev.jalves.estg.trabalhopratico.objects

data class Task (

    val projectid: String,
    val taskid: String,
    val name: String,
    val description: String,
    val status: String,
    val createdAt: String,
    val updatedBy: String,
    val createdBy: String
    )