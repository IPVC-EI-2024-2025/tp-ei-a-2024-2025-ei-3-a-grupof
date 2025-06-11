package dev.jalves.estg.trabalhopratico.services

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.lazy.items
import dev.jalves.estg.trabalhopratico.dto.CreateUserDTO
import dev.jalves.estg.trabalhopratico.services.SupabaseAdminService.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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
                    }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create user", e)
                Result.failure(e)
            }
        }


    suspend fun updateUser(id:String, user: CreateUserDTO): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                SupabaseAdminService.initAdminSession()

                supabase.auth.admin.updateUserById(uid =id) {
                    userMetadata = buildJsonObject {
                        put("username", user.username)
                        put("display_name", user.displayName)
                        put("profile_picture", "")

                    }

                }

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create user", e)
                Result.failure(e)
            }
        }

    suspend fun getUsers(): Result<List<UserInfo>> =
        withContext(Dispatchers.IO) {
            try {
                SupabaseAdminService.initAdminSession()

                val result = adminClient.auth.admin.retrieveUsers()
                val goodUsers = mutableListOf<UserInfo>()

                result.forEach { resultado ->
                    val userMetadata = resultado.userMetadata?.jsonObject
                    if (userMetadata?.containsKey("Status") != true) {
                        goodUsers.add(resultado
                        )
                    }
                }
                Result.success(goodUsers)
            } catch (e: Exception) {
                Log.e("UserCrud", "Failed to retrieve users", e)
                Result.failure(e)
            }
        }

    suspend fun disableUser(id: String): Unit =
        withContext(Dispatchers.IO) {
            try {
                SupabaseAdminService.initAdminSession()

                supabase.auth.admin.updateUserById(uid = id) {
                    userMetadata = buildJsonObject {
                        put("Status", "Disabled")
                    }
                }
            }catch (e: Exception){
                Log.e(TAG, "Failed to create user", e)
            }

        }

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}
