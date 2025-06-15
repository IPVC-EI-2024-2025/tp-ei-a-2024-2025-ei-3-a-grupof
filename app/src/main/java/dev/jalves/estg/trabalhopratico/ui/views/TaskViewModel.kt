package dev.jalves.estg.trabalhopratico.ui.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jalves.estg.trabalhopratico.objects.Task
import dev.jalves.estg.trabalhopratico.objects.User
import dev.jalves.estg.trabalhopratico.services.ProjectService
import dev.jalves.estg.trabalhopratico.services.TaskService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel : ViewModel() {
    private val _task = MutableStateFlow<Task?>(null)
    val task: StateFlow<Task?> = _task

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _projectName = MutableStateFlow<String?>(null)
    val projectName: StateFlow<String?> = _projectName

    private val _assignedEmployees = MutableStateFlow<List<User>>(emptyList())
    val assignedEmployees: StateFlow<List<User>> = _assignedEmployees

    fun loadTask(taskId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = TaskService.getTaskByID(taskId)
                result.fold(
                    onSuccess = { task ->
                        _task.value = task
                        _error.value = null
                        loadProjectName(task.projectId)
                        loadTaskEmployees(task.id)
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _task.value = null
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message
                _task.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadProjectName(projectId: String) {
        viewModelScope.launch {
            try {
                val result = ProjectService.getProjectByID(projectId)
                result.fold(
                    onSuccess = { project ->
                        _projectName.value = project.name
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun loadTaskEmployees(taskId: String) {
        viewModelScope.launch {
            try {
                val result = TaskService.getTaskEmployees(taskId)
                result.fold(
                    onSuccess = { employees ->
                        _assignedEmployees.value = employees
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}