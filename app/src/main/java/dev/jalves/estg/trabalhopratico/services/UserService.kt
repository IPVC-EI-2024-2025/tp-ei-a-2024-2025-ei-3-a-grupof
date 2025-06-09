package dev.jalves.estg.trabalhopratico.services

import dev.jalves.estg.trabalhopratico.dto.UpdateUserDTO
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import io.github.jan.supabase.auth.auth
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
}