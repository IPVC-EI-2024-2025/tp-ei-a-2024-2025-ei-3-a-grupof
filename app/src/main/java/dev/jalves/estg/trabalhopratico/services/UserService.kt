package dev.jalves.estg.trabalhopratico.services

import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.util.Log
import dev.jalves.estg.trabalhopratico.dto.CreateUserDTO
import dev.jalves.estg.trabalhopratico.dto.UpdateUserDTO
import dev.jalves.estg.trabalhopratico.objects.Role
import dev.jalves.estg.trabalhopratico.objects.User
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Duration.Companion.minutes

object UserService {
    private val adminClient = SupabaseAdminService.supabase

    suspend fun createUser(user: CreateUserDTO): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                adminClient.auth.signUpWith(Email) {
                    email = user.email
                    password = user.password
                    data = buildJsonObject {
                        put("username", user.username)
                        put("display_name", user.displayName)
                        put("profile_picture", "")
                        put("role", user.role.value)
                        put("status", true)
                    }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create user", e)
                Result.failure(e)
            }
        }


    suspend fun updateUser(id: String, user: CreateUserDTO): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                SupabaseAdminService.initAdminSession()

                SupabaseAdminService.supabase.auth.admin.updateUserById(uid = id) {
                    userMetadata = buildJsonObject {
                        put("username", user.username)
                        put("display_name", user.displayName)
                        put("profile_picture", "")
                        put("role", user.role.value)
                    }
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update user", e)
                Result.failure(e)
            }
        }


    suspend fun setUserStatus(id: String, status: Boolean): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                SupabaseAdminService.initAdminSession()

                SupabaseAdminService.supabase.auth.admin.updateUserById(uid = id) {
                    userMetadata = buildJsonObject {
                        put("status", !status)
                    }
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to disable user", e)
                Result.failure(e)
            }
        }

    suspend fun getUsers(): Result<List<User>> =
        withContext(Dispatchers.IO) {
            try {
                val result = supabase.from("users")
                    .select {
                    }.decodeList<User>()

                Result.success(result)
            } catch (e: Exception) {
                Log.e("UserService", "Failed to retrieve users", e)
                Result.failure(e)
            }
        }

    suspend fun updateUserInfo(updatedUser: UpdateUserDTO) {
        supabase.auth
            .updateUser {
                if (updatedUser.email != null) email = updatedUser.email
                if (updatedUser.password != null) password = updatedUser.password
                data {
                    if (updatedUser.displayName != null) put(
                        "display_name",
                        updatedUser.displayName
                    )
                    if (updatedUser.username != null) put("username", updatedUser.username)
                    if (updatedUser.profilePicture != null) put(
                        "profile_picture",
                        updatedUser.profilePicture
                    )
                    if (updatedUser.role != null) put("role", updatedUser.role.value)
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

    suspend fun fetchUsersByQuery(query: String, role: Role? = null, status: Boolean? = null): List<User> {
        return supabase.from("users").select {
            filter {
                if (role != null) {
                    eq("role", role.value)
                }
                if (status != null) {
                    eq("status", status)
                }
                if (query.isNotBlank()) {
                    or {
                        ilike("display_name", "%$query%")
                        ilike("email", "%$query%")
                        ilike("username", "%$query%")
                    }
                }
            }
        }.decodeList<User>()
    }

    suspend fun fetchUserById(id: String): User {
        return supabase.from("users").select {
            filter {
                eq("id", id)
            }
        }.decodeSingle()
    }

    suspend fun getCurrentUserRole(): Role {
        return try {
            val currentUser = supabase.auth.currentUserOrNull()
            val roleString = currentUser?.userMetadata?.get("role")?.toString()?.replace("\"", "")
            roleString?.let { Role.fromString(it) } ?: Role.EMPLOYEE
        } catch (e: Exception) {
            Log.e("UserService", "Failed to get current user role", e)
            Role.EMPLOYEE
        }
    }
}