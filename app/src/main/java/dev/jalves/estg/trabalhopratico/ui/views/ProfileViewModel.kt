package dev.jalves.estg.trabalhopratico.ui.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jalves.estg.trabalhopratico.objects.TaskSyncUser
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class ProfileViewModel : ViewModel() {
    private val _profile = MutableStateFlow<TaskSyncUser?>(null)
    val profile: StateFlow<TaskSyncUser?> = _profile

    init {
        viewModelScope.launch {
            supabase.auth.awaitInitialization()

            val userId = supabase.auth.currentUserOrNull()?.id ?: return@launch

            val profile = supabase.from("users")
                .select{
                    filter {
                        eq("uid", userId)
                    }
                    limit(count = 1)
                }
                .decodeSingle<TaskSyncUser>()

            profile.email = supabase.auth.currentUserOrNull()!!.email ?: ""

            _profile.value = profile
        }
    }
}