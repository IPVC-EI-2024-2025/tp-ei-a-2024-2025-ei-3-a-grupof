package dev.jalves.estg.trabalhopratico.ui.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jalves.estg.trabalhopratico.objects.User
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class ProfileViewModel : ViewModel() {
    private val _profile = MutableStateFlow<User?>(null)
    val profile: StateFlow<User?> = _profile

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            supabase.auth.awaitInitialization()

            val user = supabase.auth.currentUserOrNull() ?: return@launch

            _profile.value = User(
                id = user.id,
                email = user.email ?: "",
                displayName = user.userMetadata!!.getValue("display_name").toString().removeSurrounding("\""),
                username = user.userMetadata!!.getValue("username").toString().removeSurrounding("\"")
            )
        }
    }
}