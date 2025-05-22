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
    } catch (e: Exception) {
        null
    }
}