package dev.jalves.estg.trabalhopratico.ui.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jalves.estg.trabalhopratico.dto.ProjectDTO
import dev.jalves.estg.trabalhopratico.services.ProjectService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProjectViewModel : ViewModel() {
    private val _project = MutableStateFlow<ProjectDTO?>(null)
    val project: StateFlow<ProjectDTO?> = _project

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadProject(projectId: String) {
        viewModelScope.launch {
            val result = ProjectService.getProjectByID(projectId)
            result.onSuccess {
                _project.value = it
            }.onFailure {
                _error.value = it.message
            }
        }
    }
}