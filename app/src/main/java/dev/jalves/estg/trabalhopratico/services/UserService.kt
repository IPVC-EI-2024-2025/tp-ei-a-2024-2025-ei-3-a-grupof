package dev.jalves.estg.trabalhopratico.services

import dev.jalves.estg.trabalhopratico.dto.UpdateUserDTO
import dev.jalves.estg.trabalhopratico.objects.TaskSyncUser
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.put

object UserService {
    suspend fun updateUserInfo(updatedUser: UpdateUserDTO) {
        supabase.auth
            .updateUser{
                if(updatedUser.email != null) email = updatedUser.email
                if(updatedUser.password != null) password = updatedUser.password
                data {
                    if(updatedUser.displayName != null) put("display_name", updatedUser.displayName)
                    if(updatedUser.username != null) put("username", updatedUser.username)
                    if(updatedUser.profilePicture != null) put("profile_picture", updatedUser.profilePicture)
                }
            }
    }

    suspend fun fetchUsers(query: String): List<TaskSyncUser> {
        return supabase.from("users").select {
            filter {
                ilike("display_name", "%$query%")
            }
        }.decodeList<TaskSyncUser>()
    }
}