package dev.jalves.estg.trabalhopratico.ui.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jalves.estg.trabalhopratico.objects.Project
import dev.jalves.estg.trabalhopratico.services.ProjectService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProjectsViewModel : ViewModel() {
    private val _projects = MutableStateFlow<List<Project>?>(null)
    val projects: StateFlow<List<Project>?> = _projects

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            val result = ProjectService.listProjects()
            result.onSuccess { projectList ->
                _projects.value = projectList
            }.onFailure { exception ->
                _errorMessage.value = exception.message
            }
        }
    }
}