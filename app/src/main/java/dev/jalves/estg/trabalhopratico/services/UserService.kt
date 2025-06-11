package dev.jalves.estg.trabalhopratico.services

import android.content.Context
import android.net.Uri
import android.util.Log
import dev.jalves.estg.trabalhopratico.dto.UpdateUserDTO
import dev.jalves.estg.trabalhopratico.objects.TaskSyncUser
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.serialization.json.put
import kotlin.time.Duration.Companion.minutes

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

    suspend fun getProfilePictureURL(pictureSize: Int, userId: String): String {
        val bucket = supabase.storage.from("profile-pictures")
        return bucket.createSignedUrl(path = userId, expiresIn = 3.minutes) {
            size(pictureSize, pictureSize)
            fill()
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