package dev.jalves.estg.trabalhopratico.services

import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.util.Log
import dev.jalves.estg.trabalhopratico.dto.CreateTaskLogDTO
import dev.jalves.estg.trabalhopratico.objects.TaskLog
import dev.jalves.estg.trabalhopratico.objects.LogPhotos
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

object TaskLogService {
    suspend fun createTaskLog(taskLogDto: CreateTaskLogDTO): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val currentUserId = supabase.auth.currentUserOrNull()?.id
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val taskLogId = UUID.randomUUID().toString()

                val formattedDate = formatDateForDatabase(taskLogDto.date)
                    ?: return@withContext Result.failure(Exception("Invalid date format"))

                val taskLog = TaskLog(
                    id = taskLogId,
                    userId = currentUserId,
                    taskId = taskLogDto.taskId,
                    date = formattedDate,
                    location = taskLogDto.location,
                    completionRate = taskLogDto.completionRate,
                    timeSpent = taskLogDto.timeSpent,
                    notes = taskLogDto.notes
                )

                supabase.from("task_logs").insert(taskLog)

                Result.success(taskLogId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create task log", e)
                Result.failure(e)
            }
        }

    private fun formatDateForDatabase(dateString: String): String? {
        return try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

            val date = inputFormat.parse(dateString) ?: return null
            outputFormat.format(date)
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting date", e)
            null
        }
    }

    suspend fun getTaskLogsByTaskId(taskId: String): Result<List<TaskLog>> =
        withContext(Dispatchers.IO) {
            try {
                val taskLogs = supabase.from("task_logs")
                    .select {
                        filter {
                            eq("task_id", taskId)
                        }
                    }
                    .decodeList<TaskLog>()

                Result.success(taskLogs)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch task logs", e)
                Result.failure(e)
            }
        }

    suspend fun getTaskLogById(logId: String): Result<TaskLog> =
        withContext(Dispatchers.IO) {
            try {
                val taskLog = supabase.from("task_logs")
                    .select {
                        filter {
                            eq("id", logId)
                        }
                    }
                    .decodeSingle<TaskLog>()

                Result.success(taskLog)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch task log", e)
                Result.failure(e)
            }
        }

    suspend fun uploadLogPhotos(
        logId: String,
        uris: List<Uri>,
        context: Context
    ): Result<List<String>> =
        withContext(Dispatchers.IO) {
            try {
                val currentUserId = supabase.auth.currentUserOrNull()?.id
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val uploadedUrls = mutableListOf<String>()

                uris.forEachIndexed { index, uri ->
                    val byteArray = context.contentResolver.openInputStream(uri)?.use { stream ->
                        stream.readBytes()
                    } ?: throw Exception("Failed to read image at index $index")

                    val fileName = "${currentUserId}/${logId}-${index + 1}.jpg"

                    supabase.storage.from("log-photos").upload(fileName, byteArray) {
                        upsert = true
                        contentType = ContentType.Image.JPEG
                    }

                    val photoUrl = supabase.storage.from("log-photos")
                        .createSignedUrl(fileName, 365.minutes)

                    val logPhoto = LogPhotos(
                        id = "${logId}-${index + 1}",
                        photoUrl = photoUrl,
                        logId = logId
                    )

                    supabase.from("log_photos").insert(logPhoto)
                    uploadedUrls.add(photoUrl)
                }

                Result.success(uploadedUrls)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload log photos", e)
                Result.failure(e)
            }
        }

    suspend fun uploadLogPhoto(
        logId: String,
        uri: Uri,
        context: Context
    ): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val currentUserId = supabase.auth.currentUserOrNull()?.id
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val existingPhotos = supabase.from("log_photos")
                    .select {
                        filter {
                            eq("log_id", logId)
                        }
                    }
                    .decodeList<LogPhotos>()

                val nextNumber = existingPhotos.size + 1

                val byteArray = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.readBytes()
                } ?: return@withContext Result.failure(Exception("Failed to read image"))

                val fileName = "${currentUserId}/${logId}-${nextNumber}.jpg"

                supabase.storage.from("log-photos").upload(fileName, byteArray) {
                    upsert = true
                    contentType = ContentType.Image.JPEG
                }

                val photoUrl = supabase.storage.from("log-photos")
                    .createSignedUrl(fileName, 365.minutes)

                val logPhoto = LogPhotos(
                    id = "${logId}-${nextNumber}",
                    photoUrl = photoUrl,
                    logId = logId
                )

                supabase.from("log_photos").insert(logPhoto)

                Result.success(photoUrl)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload log photo", e)
                Result.failure(e)
            }
        }

    suspend fun getLogPhotos(logId: String): Result<List<LogPhotos>> =
        withContext(Dispatchers.IO) {
            try {
                val photos = supabase.from("log_photos")
                    .select {
                        filter {
                            eq("log_id", logId)
                        }
                    }
                    .decodeList<LogPhotos>()

                Result.success(photos)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch log photos", e)
                Result.failure(e)
            }
        }

    suspend fun deleteTaskLog(logId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                supabase.from("log_photos").delete {
                    filter {
                        eq("log_id", logId)
                    }
                }

                supabase.from("task_logs").delete {
                    filter {
                        eq("id", logId)
                    }
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete task log", e)
                Result.failure(e)
            }
        }
}