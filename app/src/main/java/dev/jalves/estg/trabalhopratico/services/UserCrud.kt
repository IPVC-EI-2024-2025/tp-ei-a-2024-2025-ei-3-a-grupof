package dev.jalves.estg.trabalhopratico.services

import android.content.ContentValues.TAG
import android.util.Log
import dev.jalves.estg.trabalhopratico.dto.CreateUserDTO
import dev.jalves.estg.trabalhopratico.services.SupabaseAdminService.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object UserCrud {
    private val adminClient = supabase

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
                        put("status", "Enabled")
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

                supabase.auth.admin.updateUserById(uid = id) {
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

    suspend fun disableUser(id: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                SupabaseAdminService.initAdminSession()

                supabase.auth.admin.updateUserById(uid = id) {
                    userMetadata = buildJsonObject {
                        put("status", "Disabled")
                    }
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to disable user", e)
                Result.failure(e)
            }
        }

    suspend fun enableUser(id: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                SupabaseAdminService.initAdminSession()

                supabase.auth.admin.updateUserById(uid = id) {
                    userMetadata = buildJsonObject {
                        put("status", "Enabled")
                    }
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to enable user", e)
                Result.failure(e)
            }
        }
}