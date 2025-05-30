package dev.jalves.estg.trabalhopratico.ui.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jalves.estg.trabalhopratico.objects.TaskSyncUser
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class ProfileViewModel : ViewModel() {
    private val _profile = MutableStateFlow<TaskSyncUser?>(null)
    val profile: StateFlow<TaskSyncUser?> = _profile

    init {
        viewModelScope.launch {
            supabase.auth.awaitInitialization()

            val user = supabase.auth.currentUserOrNull()

            if (user == null) return@launch

            _profile.value = TaskSyncUser(
                uid = user.id,
                email = user.email ?: "",
                displayName = user.userMetadata!!.getValue("display_name").toString(),
                username = user.userMetadata!!.getValue("username").toString(),
                profilePicture = user.userMetadata!!.getValue("profile_picture").toString(),
            )
        }
    }
}