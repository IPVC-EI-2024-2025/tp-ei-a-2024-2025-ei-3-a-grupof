package dev.jalves.estg.trabalhopratico.services

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import dev.jalves.estg.trabalhopratico.dto.CreateProjectDTO
import dev.jalves.estg.trabalhopratico.dto.ProjectDTO
import dev.jalves.estg.trabalhopratico.dto.ProjectOverviewDTO
import dev.jalves.estg.trabalhopratico.dto.UpdateProjectDTO
import dev.jalves.estg.trabalhopratico.objects.EmployeeProject
import dev.jalves.estg.trabalhopratico.objects.Project
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ProjectService {
    suspend fun createProject(projectDto: CreateProjectDTO): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val currentUserId = supabase.auth.currentUserOrNull()?.id
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val project = Project(
                    name = projectDto.name,
                    description = projectDto.description,
                    startDate = projectDto.startDate,
                    dueDate = projectDto.dueDate,
                    status = "unknown",
                    createdByID = currentUserId,
                    managerID = projectDto.managerID
                )

                supabase.from("projects").insert(project)

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create project", e)
                Result.failure(e)
            }
        }


    suspend fun addEmployeeToProject(userID:String, projectId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {

                val employeeProject = EmployeeProject(
                    userId = userID,
                    projectId = projectId
                )

                supabase.from("employee_project").insert(employeeProject)

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to Asssign Project to Employee", e)
                Result.failure(e)
            }
        }


    suspend fun removeEmployeeFromProject(userID: String, projectId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                supabase.from("employee_project")
                    .delete {
                        filter {
                            eq("user_id", userID)
                            eq("project_id", projectId)
                        }
                    }

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove employee $userID from project $projectId", e)
                Result.failure(e)
            }
        }

    suspend fun listProjects(): Result<List<Project>> =
        withContext(Dispatchers.IO) {
            try {
                val projects = supabase.from("projects")
                    .select {
                    }
                    .decodeList<Project>()

                Result.success(projects)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch projects", e)
                Result.failure(e)
            }
        }

    suspend fun listProjectsByCreator(creatorId: String): Result<List<Project>> =
        withContext(Dispatchers.IO) {
            try {
                val projects = supabase.from("projects")
                    .select {
                        filter {
                            eq("created_by_id", creatorId)
                        }
                    }
                    .decodeList<Project>()

                Result.success(projects)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch projects for creator $creatorId", e)
                Result.failure(e)
            }
        }

    suspend fun getProjectByID(projectID: String): Result<ProjectDTO> =
        withContext(Dispatchers.IO) {
            try {
                val project = supabase.postgrest.rpc("get_project", parameters = buildJsonObject {
                    put("p_id", projectID)
                })

                Log.d("PROJECT", project.data)

                Result.success(project.decodeAs<ProjectDTO>())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch project with ID $projectID", e)
                Result.failure(e)
            }
        }

    suspend fun updateProject(updatedProject: UpdateProjectDTO) =
        withContext(Dispatchers.IO) {
            try {
                Log.d("PROJECT", updatedProject.id)

                supabase.from("projects").update(
                    buildMap {
                        updatedProject.name?.let { put("name", it) }
                        updatedProject.description?.let { put("description", it) }
                        updatedProject.startDate?.let { put("start_date", it) }
                        updatedProject.dueDate?.let { put("due_date", it) }
                        updatedProject.status?.let { put("status", it) }
                        updatedProject.managerID?.let { put("manager_id", it) }
                    }
                ) {
                    filter {
                        eq("id", updatedProject.id)
                    }
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update project", e)
                Result.failure(e)
            }
        }

    suspend fun listProjectsForEmployee(employeeId: String): Result<List<ProjectDTO>> =
        withContext(Dispatchers.IO) {
            try {
                val result = supabase.postgrest.rpc(
                    "get_employee_projects",
                    parameters = buildJsonObject {
                        put("employee_id", employeeId)
                    }
                )

                val projects = result.decodeList<ProjectDTO>()

                Result.success(projects)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch projects for employee $employeeId", e)
                Result.failure(e)
            }
        }

    suspend fun getProjectOverview(projectId: String): Result<ProjectOverviewDTO> =
        withContext(Dispatchers.IO) {
            try {
                val overview = supabase.postgrest.rpc(
                    function = "get_project_info",
                    parameters = buildJsonObject {
                        put("p_project_id", projectId)
                    }
                )

                Log.d("PROJECT_OVERVIEW", overview.data)

                Result.success(overview.decodeAs<ProjectOverviewDTO>())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch project overview for ID $projectId", e)
                Result.failure(e)
            }
        }

    suspend fun exportProjectStatsToPDF(
        context: Context,
        projectId: String,
        onSuccess: (File) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val overviewResult = getProjectOverview(projectId)

            if (overviewResult.isFailure) {
                withContext(Dispatchers.Main) {
                    onError("Failed to fetch project data: ${overviewResult.exceptionOrNull()?.message}")
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
            var pageNumber = 1

            fun checkPageSpace(additionalHeight: Float) {
                if (yPosition + additionalHeight > 800) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                    page = pdfDocument.startPage(newPageInfo)
                    canvas = page.canvas
                    yPosition = 80f
                }
            }

            canvas.drawText("Project Statistics Report", leftMargin, yPosition, titlePaint)
            yPosition += 40f

            canvas.drawLine(leftMargin, yPosition, 545f, yPosition, linePaint)
            yPosition += 30f

            canvas.drawText("Project Information", leftMargin, yPosition, headerPaint)
            yPosition += 30f

            canvas.drawText("Name: ${overview.project.name}", leftMargin + 20f, yPosition, bodyPaint)
            yPosition += lineSpacing

            canvas.drawText("Description: ${overview.project.description}", leftMargin + 20f, yPosition, bodyPaint)
            yPosition += lineSpacing

            canvas.drawText("Status: ${overview.project.status}", leftMargin + 20f, yPosition, bodyPaint)
            yPosition += lineSpacing

            canvas.drawText("Start Date: ${overview.project.startDate}", leftMargin + 20f, yPosition, bodyPaint)
            yPosition += lineSpacing

            canvas.drawText("Due Date: ${overview.project.dueDate}", leftMargin + 20f, yPosition, bodyPaint)
            yPosition += lineSpacing

            overview.project.manager?.let { manager ->
                canvas.drawText("Manager: ${manager.displayName} (${manager.username})", leftMargin + 20f, yPosition, bodyPaint)
                yPosition += lineSpacing
            }

            yPosition += 20f

            checkPageSpace(60f)
            canvas.drawText("Project Team", leftMargin, yPosition, headerPaint)
            yPosition += 30f

            if (overview.employees?.isEmpty() == true) {
                canvas.drawText("No employees assigned to this project.", leftMargin + 20f, yPosition, bodyPaint)
                yPosition += lineSpacing
            } else {
                overview.employees?.forEach { employee ->
                    checkPageSpace(lineSpacing * 4)
                    canvas.drawText("• ${employee.displayName} (${employee.username})", leftMargin + 20f, yPosition, bodyPaint)
                    yPosition += lineSpacing
                    canvas.drawText("  Email: ${employee.email}", leftMargin + 40f, yPosition, bodyPaint)
                    yPosition += lineSpacing
                    canvas.drawText("  Assigned: ${employee.assignedAt ?: "N/A"}", leftMargin + 40f, yPosition, bodyPaint)
                    yPosition += lineSpacing + 10f
                }
            }

            yPosition += 20f

            checkPageSpace(60f)
            canvas.drawText("Project Tasks", leftMargin, yPosition, headerPaint)
            yPosition += 30f

            if (overview.tasks?.isEmpty() == true) {
                canvas.drawText("No tasks available for this project.", leftMargin + 20f, yPosition, bodyPaint)
                yPosition += lineSpacing
            } else {
                overview.tasks?.forEachIndexed { index, task ->
                    checkPageSpace(lineSpacing * 8)

                    canvas.drawText("Task #${index + 1}: ${task.name}", leftMargin + 20f, yPosition, headerPaint)
                    yPosition += lineSpacing + 5f

                    canvas.drawText("Description: ${task.description}", leftMargin + 40f, yPosition, bodyPaint)
                    yPosition += lineSpacing

                    canvas.drawText("Status: ${task.status}", leftMargin + 40f, yPosition, bodyPaint)
                    yPosition += lineSpacing

                    canvas.drawText("Created: ${task.createdAt ?: "N/A"}", leftMargin + 40f, yPosition, bodyPaint)
                    yPosition += lineSpacing

                    if (task.assignedEmployees?.isNotEmpty() == true) {
                        val employeeNames = task.assignedEmployees.joinToString(", ") { it.displayName }
                        canvas.drawText("Assigned to: $employeeNames", leftMargin + 40f, yPosition, bodyPaint)
                        yPosition += lineSpacing
                    }

                    val logCount = task.taskLogs?.size ?: 0
                    if (logCount > 0) {
                        val totalTimeSpent = task.taskLogs?.sumOf { it.timeSpent } ?: 0.0
                        val avgCompletion = task.taskLogs?.map { it.completionRate }?.average() ?: 0.0

                        canvas.drawText("Logs: $logCount entries, ${String.format("%.1f", totalTimeSpent)}h total, ${String.format("%.1f", avgCompletion)}% avg completion",
                            leftMargin + 40f, yPosition, bodyPaint)
                        yPosition += lineSpacing
                    }

                    yPosition += 15f
                }
            }

            yPosition += 20f
            checkPageSpace(120f)
            canvas.drawLine(leftMargin, yPosition, 545f, yPosition, linePaint)
            yPosition += 30f

            canvas.drawText("Project Summary", leftMargin, yPosition, headerPaint)
            yPosition += 30f

            val totalTasks = overview.tasks?.size ?: 0
            val completedTasks = overview.tasks?.count { it.status.equals("completed", ignoreCase = true) } ?: 0
            val inProgressTasks = overview.tasks?.count { it.status.equals("in_progress", ignoreCase = true) } ?: 0
            val totalEmployees = overview.employees?.size ?: 0
            val totalTimeSpent = overview.tasks?.flatMap { it.taskLogs ?: emptyList() }?.sumOf { it.timeSpent } ?: 0.0

            canvas.drawText("Total Tasks: $totalTasks", leftMargin + 20f, yPosition, bodyPaint)
            yPosition += lineSpacing

            canvas.drawText("Completed Tasks: $completedTasks", leftMargin + 20f, yPosition, bodyPaint)
            yPosition += lineSpacing

            canvas.drawText("In Progress Tasks: $inProgressTasks", leftMargin + 20f, yPosition, bodyPaint)
            yPosition += lineSpacing

            canvas.drawText("Team Members: $totalEmployees", leftMargin + 20f, yPosition, bodyPaint)
            yPosition += lineSpacing

            canvas.drawText("Total Time Logged: ${String.format("%.1f", totalTimeSpent)} hours", leftMargin + 20f, yPosition, bodyPaint)
            yPosition += lineSpacing

            if (totalTasks > 0) {
                val completionPercentage = (completedTasks.toDouble() / totalTasks.toDouble()) * 100
                canvas.drawText("Project Completion: ${String.format("%.1f", completionPercentage)}%", leftMargin + 20f, yPosition, bodyPaint)
                yPosition += lineSpacing
            }

            yPosition += 30f
            checkPageSpace(60f)
            canvas.drawLine(leftMargin, yPosition, 545f, yPosition, linePaint)
            yPosition += 30f

            val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                Date()
            )
            canvas.drawText("Export Date: $currentDate", leftMargin, yPosition, bodyPaint)

            pdfDocument.finishPage(page)

            val fileName = "Project_stats_${overview.project.name.replace(" ", "_")}_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }

            pdfDocument.close()

            Log.d("ProjectService", "PDF exported successfully: ${file.absolutePath}")

            withContext(Dispatchers.Main) {
                onSuccess(file)
            }

        } catch (e: Exception) {
            Log.e("ProjectService", "Failed to export project PDF", e)
            withContext(Dispatchers.Main) {
                onError("Failed to export PDF: ${e.message}")
            }
        }
    }


}


