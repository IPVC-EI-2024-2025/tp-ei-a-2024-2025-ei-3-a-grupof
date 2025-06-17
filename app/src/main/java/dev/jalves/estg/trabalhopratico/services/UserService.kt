package dev.jalves.estg.trabalhopratico.services

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.util.Log
import dev.jalves.estg.trabalhopratico.dto.CreateUserDTO
import dev.jalves.estg.trabalhopratico.dto.CreateUserPerformanceDTO
import dev.jalves.estg.trabalhopratico.dto.UpdateUserDTO
import dev.jalves.estg.trabalhopratico.dto.UserOverviewDTO
import dev.jalves.estg.trabalhopratico.objects.Role
import dev.jalves.estg.trabalhopratico.objects.User
import dev.jalves.estg.trabalhopratico.objects.UserPerformance
import dev.jalves.estg.trabalhopratico.services.AuthService.validateUserData
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
                val validationError = validateUserData(user)
                if (validationError != null) {
                    return@withContext Result.failure(Exception(validationError))
                }

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
                val existing = fetchUserById(user.id)
                val temporaryUser = CreateUserDTO(
                    displayName = user.displayName ?: existing.displayName,
                    email       = user.email       ?: existing.email,
                    username    = user.username    ?: existing.username,
                    password    = "",
                    role        = user.role        ?: existing.role
                )

                val validationError = validateUserData(temporaryUser, user.id)
                if (validationError != null) {
                    return@withContext Result.failure(Exception(validationError))
                }

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

    suspend fun fetchUsersByQuery(
        query: String,
        role: Role? = null,
        status: Boolean? = null
    ): List<User> {
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
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

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

            val subHeaderPaint = Paint().apply {
                color = Color.BLACK
                textSize = 16f
                isFakeBoldText = true
            }

            val bodyPaint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
            }

            val smallPaint = Paint().apply {
                color = Color.GRAY
                textSize = 10f
            }

            val linePaint = Paint().apply {
                color = Color.GRAY
                strokeWidth = 1f
            }

            var yPosition = 80f
            val leftMargin = 50f
            val lineSpacing = 20f
            val smallLineSpacing = 15f
            var pageNumber = 1

            fun checkNewPage(): Boolean {
                if (yPosition > 750f) {
                    canvas.drawText("Page $pageNumber", 500f, 820f, smallPaint)
                    pdfDocument.finishPage(page)

                    pageNumber++
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = 80f
                    return true
                }
                return false
            }

            canvas.drawText("User Statistics Report", leftMargin, yPosition, titlePaint)
            yPosition += 40f

            canvas.drawLine(leftMargin, yPosition, 545f, yPosition, linePaint)
            yPosition += 30f

            canvas.drawText("User Information", leftMargin, yPosition, headerPaint)
            yPosition += 30f

            canvas.drawText("Display Name: ${overview.user.displayName ?: "N/A"}", leftMargin + 20f, yPosition, bodyPaint)
            yPosition += lineSpacing

            canvas.drawText("Username: ${overview.user.username ?: "N/A"}", leftMargin + 20f, yPosition, bodyPaint)
            yPosition += lineSpacing

            canvas.drawText("Email: ${overview.user.email}", leftMargin + 20f, yPosition, bodyPaint)
            yPosition += lineSpacing

            canvas.drawText("Role: ${overview.user.role}", leftMargin + 20f, yPosition, bodyPaint)
            yPosition += lineSpacing

            canvas.drawText("Status: ${if (overview.user.status == true) "Active" else "Inactive"}", leftMargin + 20f, yPosition, bodyPaint)
            yPosition += 40f

            checkNewPage()

            if (overview.performanceReviews.isNotEmpty()) {
                canvas.drawText("Performance Summary", leftMargin, yPosition, headerPaint)
                yPosition += 30f

                val totalReviews = overview.performanceReviews.size
                val ratingsWithValues = overview.performanceReviews.mapNotNull { it.rating }
                val averageRating = if (ratingsWithValues.isNotEmpty()) ratingsWithValues.average() else null
                val highestRating = ratingsWithValues.maxOrNull()
                val lowestRating = ratingsWithValues.minOrNull()

                canvas.drawText("Total Reviews: $totalReviews", leftMargin + 20f, yPosition, bodyPaint)
                yPosition += lineSpacing

                averageRating?.let { rating ->
                    canvas.drawText("Average Rating: ${"%.2f".format(rating)}/5.0", leftMargin + 20f, yPosition, bodyPaint)
                    yPosition += lineSpacing
                }

                highestRating?.let { rating ->
                    canvas.drawText("Highest Rating: ${"%.2f".format(rating)}", leftMargin + 20f, yPosition, bodyPaint)
                    yPosition += lineSpacing
                }

                lowestRating?.let { rating ->
                    canvas.drawText("Lowest Rating: ${"%.2f".format(rating)}", leftMargin + 20f, yPosition, bodyPaint)
                    yPosition += lineSpacing
                }

                yPosition += 30f
            }

            checkNewPage()

            if (overview.tasks.isNotEmpty()) {
                canvas.drawText("Task Summary", leftMargin, yPosition, headerPaint)
                yPosition += 30f

                val totalTasks = overview.tasks.size
                val completedTasks = overview.tasks.count { it.completionRate == 1.0 }
                val inProgressTasks = overview.tasks.count { (it.completionRate ?: 0.0) > 0 && (it.completionRate ?: 0.0) < 1.0 }
                val notStartedTasks = overview.tasks.count { it.completionRate == 0.0 }
                val averageCompletionRate = overview.tasks.mapNotNull { it.completionRate }.average().takeIf { !it.isNaN() }

                canvas.drawText("Total Tasks: $totalTasks", leftMargin + 20f, yPosition, bodyPaint)
                yPosition += lineSpacing

                canvas.drawText("Completed Tasks: $completedTasks", leftMargin + 20f, yPosition, bodyPaint)
                yPosition += lineSpacing

                canvas.drawText("In Progress Tasks: $inProgressTasks", leftMargin + 20f, yPosition, bodyPaint)
                yPosition += lineSpacing

                canvas.drawText("Not Started Tasks: $notStartedTasks", leftMargin + 20f, yPosition, bodyPaint)
                yPosition += lineSpacing

                averageCompletionRate?.let { rate ->
                    canvas.drawText("Average Completion Rate: ${"%.1f".format(rate * 100)}%", leftMargin + 20f, yPosition, bodyPaint)
                    yPosition += lineSpacing
                }

                yPosition += 30f
            }

            checkNewPage()

            if (overview.projects.isNotEmpty()) {
                canvas.drawText("Projects (${overview.projects.size})", leftMargin, yPosition, headerPaint)
                yPosition += 30f

                overview.projects.forEachIndexed { index, project ->
                    checkNewPage()

                    canvas.drawText("${index + 1}. ${project.name ?: "Unknown Project"}",
                        leftMargin + 20f, yPosition, subHeaderPaint)
                    yPosition += lineSpacing

                    project.description?.let { desc ->
                        val words = desc.split(" ")
                        var currentLine = ""
                        words.forEach { word ->
                            if ((currentLine + word).length > 60) {
                                canvas.drawText("   $currentLine", leftMargin + 40f, yPosition, smallPaint)
                                yPosition += smallLineSpacing
                                checkNewPage()
                                currentLine = word
                            } else {
                                currentLine += if (currentLine.isEmpty()) word else " $word"
                            }
                        }
                        if (currentLine.isNotEmpty()) {
                            canvas.drawText("   $currentLine", leftMargin + 40f, yPosition, smallPaint)
                            yPosition += smallLineSpacing
                        }
                    }

                    project.status?.let { status ->
                        canvas.drawText("   Status: $status", leftMargin + 40f, yPosition, smallPaint)
                        yPosition += smallLineSpacing
                    }

                    yPosition += 25f
                }
                yPosition += 20f
            }

            checkNewPage()

            if (overview.tasks.isNotEmpty()) {
                canvas.drawText("Task Assignments (${overview.tasks.size})", leftMargin, yPosition, headerPaint)
                yPosition += 30f

                overview.tasks.take(10).forEachIndexed { index, task ->
                    checkNewPage()

                    canvas.drawText("Task ${index + 1}", leftMargin + 20f, yPosition, subHeaderPaint)
                    yPosition += lineSpacing

                    canvas.drawText("   Task ID: ${task.taskId}", leftMargin + 40f, yPosition, smallPaint)
                    yPosition += smallLineSpacing

                    task.assignedAt?.let { assignedAt ->
                        canvas.drawText("   Assigned: $assignedAt", leftMargin + 40f, yPosition, smallPaint)
                        yPosition += smallLineSpacing
                    }

                    task.completionRate?.let { rate ->
                        canvas.drawText("   Completion: ${"%.1f".format(rate * 100)}%", leftMargin + 40f, yPosition, smallPaint)
                        yPosition += smallLineSpacing
                    }

                    task.completedAt?.let { completedAt ->
                        canvas.drawText("   Completed: $completedAt", leftMargin + 40f, yPosition, smallPaint)
                        yPosition += smallLineSpacing
                    }

                    yPosition += 20f
                }
                yPosition += 20f
            }

            checkNewPage()

            if (overview.performanceReviews.isNotEmpty()) {
                canvas.drawText("Performance Reviews", leftMargin, yPosition, headerPaint)
                yPosition += 30f

                overview.performanceReviews.forEachIndexed { index, review ->
                    checkNewPage()

                    canvas.drawText("Review ${index + 1}", leftMargin + 20f, yPosition, subHeaderPaint)
                    yPosition += lineSpacing

                    review.rating?.let { rating ->
                        canvas.drawText("   Rating: ${"%.2f".format(rating)}/5.0", leftMargin + 40f, yPosition, bodyPaint)
                        yPosition += smallLineSpacing
                    }

                    review.comments?.let { comments ->
                        if (comments.isNotBlank()) {
                            canvas.drawText("   Comments:", leftMargin + 40f, yPosition, bodyPaint)
                            yPosition += smallLineSpacing

                            val words = comments.split(" ")
                            var currentLine = ""
                            words.forEach { word ->
                                if ((currentLine + word).length > 55) {
                                    canvas.drawText("     $currentLine", leftMargin + 60f, yPosition, smallPaint)
                                    yPosition += smallLineSpacing
                                    checkNewPage()
                                    currentLine = word
                                } else {
                                    currentLine += if (currentLine.isEmpty()) word else " $word"
                                }
                            }
                            if (currentLine.isNotEmpty()) {
                                canvas.drawText("     $currentLine", leftMargin + 60f, yPosition, smallPaint)
                                yPosition += smallLineSpacing
                            }
                        }
                    }
                    yPosition += 20f
                }
            }

            checkNewPage()

            if (overview.taskLogs.isNotEmpty()) {
                canvas.drawText("Task Logs (${overview.taskLogs.size})", leftMargin, yPosition, headerPaint)
                yPosition += 30f

                overview.taskLogs.take(10).forEachIndexed { index, log ->
                    checkNewPage()

                    canvas.drawText("Log ${index + 1}", leftMargin + 20f, yPosition, subHeaderPaint)
                    yPosition += lineSpacing

                    log.Location?.let { status ->
                        canvas.drawText("   Status: $status", leftMargin + 40f, yPosition, smallPaint)
                        yPosition += smallLineSpacing
                    }

                    log.Notes?.let { desc ->
                        if (desc.isNotBlank()) {
                            canvas.drawText("   Description:", leftMargin + 40f, yPosition, smallPaint)
                            yPosition += smallLineSpacing

                            val words = desc.split(" ")
                            var currentLine = ""
                            words.forEach { word ->
                                if ((currentLine + word).length > 55) {
                                    canvas.drawText("     $currentLine", leftMargin + 60f, yPosition, smallPaint)
                                    yPosition += smallLineSpacing
                                    checkNewPage()
                                    currentLine = word
                                } else {
                                    currentLine += if (currentLine.isEmpty()) word else " $word"
                                }
                            }
                            if (currentLine.isNotEmpty()) {
                                canvas.drawText("     $currentLine", leftMargin + 60f, yPosition, smallPaint)
                                yPosition += smallLineSpacing
                            }
                        }
                    }

                    yPosition += 20f
                }
            }

            checkNewPage()

            yPosition = 750f
            canvas.drawLine(leftMargin, yPosition, 545f, yPosition, linePaint)
            yPosition += 20f

            val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            canvas.drawText("Export Date: $currentDate", leftMargin, yPosition, bodyPaint)

            canvas.drawText("Page $pageNumber", 500f, 820f, smallPaint)

            pdfDocument.finishPage(page)

            val fileName = "user_stats_${overview.user.username ?: "unknown"}_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
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
    suspend fun createUserPerformance(dto: CreateUserPerformanceDTO): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val currentUserId = supabase.auth.currentUserOrNull()?.id
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val Performance = UserPerformance(
                    projectId = dto.projectId,
                    userId = dto.userId,
                    rating = dto.rating,
                    comments = dto.comments,
                    evaluatedBy = dto.evaluatedBy
                )

                supabase.from("user_performance").insert(Performance)

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create task", e)
                Result.failure(e)
            }
        }

}