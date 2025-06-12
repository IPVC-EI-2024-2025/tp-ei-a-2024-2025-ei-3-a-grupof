package dev.jalves.estg.trabalhopratico

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.formatToDateString(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(this))
}

fun String.toEpochMillis(): Long? {
    return try {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(this)?.time
    } catch (_: Exception) {
        null
    }
}

fun formatDate(input: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val date = inputFormat.parse(input)
    return outputFormat.format(date!!)
}