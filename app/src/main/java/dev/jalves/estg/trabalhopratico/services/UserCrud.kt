package dev.jalves.estg.trabalhopratico.services

import android.content.ContentValues.TAG
import android.util.Log
import dev.jalves.estg.trabalhopratico.BuildConfig
import dev.jalves.estg.trabalhopratico.dto.CreateProjectDTO
import dev.jalves.estg.trabalhopratico.dto.CreateUserDTO
import dev.jalves.estg.trabalhopratico.objects.Project
import dev.jalves.estg.trabalhopratico.services.SupabaseAdminService.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object UserCrud {
    private val adminClient = supabase

    suspend fun CreateUser(user: CreateUserDTO): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                adminClient.auth.signUpWith(Email) {
                    email = user.email
                    password = user.password
                    data = buildJsonObject {
                        put("username", user.username)
                        put("display_name", user.name)
                        put("profile_picture", "")
                    }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create user", e)
                Result.failure(e)
            }
        }


    suspend fun UodateUser(user: CreateUserDTO,ID:String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                SupabaseAdminService.initAdminSession()


                supabase.auth.admin.updateUserById(uid =ID) {
                    email = user.email
                    password = user.password
                    userMetadata = buildJsonObject {
                        put("username", user.username)
                        put("display_name", user.name)
                        put("profile_picture", "")

                    }

                }


                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create user", e)
                Result.failure(e)
            }
        }




    suspend fun GetUsers(): Result<List<UserInfo>> =
        withContext(Dispatchers.IO) {
            try {
                SupabaseAdminService.initAdminSession()

                val result = adminClient.auth.admin.retrieveUsers()
                Result.success(result)
            } catch (e: Exception) {
                Log.e("UserCrud", "Failed to retrieve users", e)
                Result.failure(e)
            }
        }

    suspend fun DisableUser(ID: String): Unit =
        withContext(Dispatchers.IO) {
            try {
                SupabaseAdminService.initAdminSession()


                supabase.auth.admin.updateUserById(uid = ID) {
                    userMetadata = buildJsonObject {
                        put("Status", "Disabled")
                    }
                }

            }catch (e: Exception){
                Log.e(TAG, "Failed to create user", e)
            }

        }

}
