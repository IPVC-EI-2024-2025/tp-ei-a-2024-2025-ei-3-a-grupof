package dev.jalves.estg.trabalhopratico.services

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.util.Log
import dev.jalves.estg.trabalhopratico.dto.CreateUserDTO
import dev.jalves.estg.trabalhopratico.dto.ProjectDTO
import dev.jalves.estg.trabalhopratico.dto.UpdateUserDTO
import dev.jalves.estg.trabalhopratico.dto.UserOverviewDTO
import dev.jalves.estg.trabalhopratico.objects.Role
import dev.jalves.estg.trabalhopratico.objects.User
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.minutes

object UserService {
    suspend fun createUser(user: CreateUserDTO): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                supabase.functions.invoke(
                    function = "create-user",
                    body = buildJsonObject {
                        put("email", user.email)
                        put("password", user.password)
                        put("user_metadata", buildJsonObject {
                            put("username", user.username)
                            put("display_name", user.displayName)
                            put("profile_picture", "")
                            put("role", user.role.value)
                            put("status", true)
                        })
                    }
                )

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create user", e)
                Result.failure(e)
            }
        }


    suspend fun updateUser(user: UpdateUserDTO): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                supabase.functions.invoke(
                    function = "edit-user",
                    body = buildJsonObject {
                        put("user_id", user.id)
                        user.email?.let { put("email", it) }
                        user.password?.let { put("password", it) }
                        put("user_metadata", buildJsonObject {
                            user.username?.let { put("username", it) }
                            user.displayName?.let { put("display_name", it) }
                            user.role?.let { put("role", it.value) }
                            user.status?.let { put("status", it) }
                        })
                    }
                )

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update user", e)
                Result.failure(e)
            }
        }

    suspend fun updateUserInfo(updatedUser: UpdateUserDTO) {
        supabase.auth
            .updateUser {
                if (updatedUser.email != null) email = updatedUser.email
                if (updatedUser.password != null) password = updatedUser.password
                data {
                    if (updatedUser.displayName != null) put(
                        "display_name",
                        updatedUser.displayName
                    )
                    if (updatedUser.username != null) put("username", updatedUser.username)
                    if (updatedUser.profilePicture != null) put(
                        "profile_picture",
                        updatedUser.profilePicture
                    )
                    if (updatedUser.role != null) put("role", updatedUser.role.value)
                    if (updatedUser.status != null) put("status", updatedUser.status)
                }
            }
    }

    suspend fun updateUserProfilePicture(
        uri: Uri,
        context: Context
    ): Boolean {
        val byteArray = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.readBytes()
        } ?: return false

        val userId = supabase.auth.currentUserOrNull()!!.id

        return try {
            supabase.storage.from("profile-pictures").upload(userId, byteArray) {
                upsert = true
                contentType = ContentType.Image.Any
            }
            Log.d("Supabase", "Profile picture uploaded for $userId")
            true
        } catch (e: Exception) {
            Log.e("Supabase", "Upload failed", e)
            false
        }
    }

    suspend fun getProfilePictureURL(pictureSize: Int, userId: String): String? {
        val bucket = supabase.storage.from("profile-pictures")
        val fileExists = bucket
            .exists(path = userId)

        if (!fileExists)
            return null

        return bucket.createSignedUrl(path = userId, expiresIn = 3.minutes) {
            size(pictureSize, pictureSize)
            fill()
        }
    }

    suspend fun fetchUsersByQuery(query: String, role: Role? = null, status: Boolean? = null): List<User> {
        return supabase.from("users").select {
            filter {
                if (role != null) {
                    eq("role", role.value)
                }
                if (status != null) {
                    eq("status", status)
                }
                if (query.isNotBlank()) {
                    or {
                        ilike("display_name", "%$query%")
                        ilike("email", "%$query%")
                        ilike("username", "%$query%")
                    }
                }
            }
        }.decodeList<User>()
    }

    suspend fun fetchUserById(id: String): User {
        return supabase.from("users").select {
            filter {
                eq("id", id)
            }
        }.decodeSingle()
    }

    fun getCurrentUserRole(): Role {
        return try {
            val currentUser = supabase.auth.currentUserOrNull()
            val roleString = currentUser?.userMetadata?.get("role")?.toString()?.replace("\"", "")
            roleString?.let { Role.fromString(it) } ?: Role.EMPLOYEE
        } catch (e: Exception) {
            Log.e("UserService", "Failed to get current user role", e)
            Role.EMPLOYEE
        }
    }
    suspend fun getUserOverview(userId: String): Result<UserOverviewDTO> =
        withContext(Dispatchers.IO) {
            try {
                val overview = supabase.postgrest.rpc(
                    function = "get_user_overview",
                    parameters = buildJsonObject {
                        put("p_user_id", userId)
                    }
                )

                Log.d("USER_OVERVIEW", overview.data)

                Result.success(overview.decodeAs<UserOverviewDTO>())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch user overview for ID $userId", e)
                Result.failure(e)
            }
        }

    suspend fun exportUserStatsToPDF(
        context: Context,
        userId: String,
        onSuccess: (File) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val overviewResult = getUserOverview(userId)

            if (overviewResult.isFailure) {
                withContext(Dispatchers.Main) {
                    onError("Failed to fetch user data: ${overviewResult.exceptionOrNull()?.message}")
                }
                return@withContext
            }

            val overview = overviewResult.getOrThrow()

            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 24f
                isFakeBoldText = true
            }

            val headerPaint = Paint().apply {
                color = Color.BLACK
                textSize = 18f
                isFakeBoldText = true
            }

            val bodyPaint = Paint().apply {
                color = Color.BLACK
                textSize = 14f
            }

            val linePaint = Paint().apply {
                color = Color.GRAY
                strokeWidth = 1f
            }

            var yPosition = 80f
            val leftMargin = 50f
            val lineSpacing = 25f




            canvas.drawText("User Statistics Report", leftMargin, yPosition, titlePaint)
            yPosition += 40f

            canvas.drawLine(leftMargin, yPosition, 545f, yPosition, linePaint)
            yPosition += 30f

            canvas.drawText("User Information", leftMargin, yPosition, headerPaint)
            yPosition += 30f

            canvas.drawText("Display Name: ${overview.user.displayName}", leftMargin + 20f, yPosition, bodyPaint)
            yPosition += lineSpacing

            canvas.drawText("Username: ${overview.user.username}", leftMargin + 20f, yPosition, bodyPaint)
            yPosition += lineSpacing

            canvas.drawText("Email: ${overview.user.email}", leftMargin + 20f, yPosition, bodyPaint)
            yPosition += lineSpacing

            canvas.drawText("Role: ${overview.user.role.value}", leftMargin + 20f, yPosition, bodyPaint)
            yPosition += 40f

            canvas.drawText("Statistics", leftMargin, yPosition, headerPaint)
            yPosition += 30f

            canvas.drawText("Number of Tasks the User is in: ${overview.taskCount}", leftMargin + 20f, yPosition, bodyPaint)
            yPosition += lineSpacing

            canvas.drawText("Number of Task Logs the User is in: ${overview.taskLogCount}", leftMargin + 20f, yPosition, bodyPaint)
            yPosition += lineSpacing

            canvas.drawText("Number of Projects the User is in: ${overview.projectCount}", leftMargin + 20f, yPosition, bodyPaint)
            yPosition += 60f

            canvas.drawLine(leftMargin, yPosition, 545f, yPosition, linePaint)
            yPosition += 30f

            val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            canvas.drawText("Export Date: $currentDate", leftMargin, yPosition, bodyPaint)

            pdfDocument.finishPage(page)

            val fileName = "user_stats_${overview.user.username}_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }

            pdfDocument.close()

            Log.d("UserService", "PDF exported successfully: ${file.absolutePath}")

            withContext(Dispatchers.Main) {
                onSuccess(file)
            }

        } catch (e: Exception) {
            Log.e("UserService", "Failed to export PDF", e)
            withContext(Dispatchers.Main) {
                onError("Failed to export PDF: ${e.message}")
            }
        }
    }
}