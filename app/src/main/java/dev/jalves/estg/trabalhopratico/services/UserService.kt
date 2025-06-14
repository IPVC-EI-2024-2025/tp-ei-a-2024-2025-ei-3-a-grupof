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
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Duration.Companion.minutes

object UserService {
    suspend fun createUser(user: CreateUserDTO): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                supabase.functions.invoke(
                    function = "create-user",
                    body = buildJsonObject {
                        put("email", user.email)
                        put("password", user.password)
                        put("user_metadata", buildJsonObject {
                            put("username", user.username)
                            put("display_name", user.displayName)
                            put("profile_picture", "")
                            put("role", user.role.value)
                            put("status", true)
                        })
                    }
                )

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create user", e)
                Result.failure(e)
            }
        }


    suspend fun updateUser(user: UpdateUserDTO): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                supabase.functions.invoke(
                    function = "edit-user",
                    body = buildJsonObject {
                        put("user_id", user.id)
                        user.email?.let { put("email", it) }
                        user.password?.let { put("password", it) }
                        put("user_metadata", buildJsonObject {
                            user.username?.let { put("username", it) }
                            user.displayName?.let { put("display_name", it) }
                            user.role?.let { put("role", it.value) }
                            user.status?.let { put("status", it) }
                        })
                    }
                )

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

    fun getCurrentUserRole(): Role {
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