package dev.jalves.estg.trabalhopratico.services

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Toast
import dev.jalves.estg.trabalhopratico.dto.CreateUserDTO
import dev.jalves.estg.trabalhopratico.dto.UpdateUserDTO
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

object AuthService {

    private suspend fun isDuplicate(key: String, value: Any): Boolean {
        return try {
            val result = supabase.from("users")
                .select {
                    filter {
                        eq(key, value)
                    }
                }
                .decodeList<Map<String, String>>()

            result.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for duplicates: ${e.message}", e)
            false
        }
    }

    private suspend fun validateUserData(newUser: CreateUserDTO): String? {
        return try {
            when {
                isDuplicate("username", newUser.username)
                    -> "duplicate_username"
                isDuplicate("email", newUser.email)
                    -> "duplicate_email"
                else -> null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for duplicates: ${e.message}", e)
            null
        }
    }

    suspend fun signUp(newUser: CreateUserDTO): Result<Unit> =
        withContext(Dispatchers.IO) {
            try{
                val error = validateUserData(newUser)
                if (error != null) {
                    return@withContext Result.failure(Exception(error))
                }

                supabase.auth.signUpWith(Email) {
                    email = newUser.email
                    password = newUser.password
                    data = buildJsonObject {
                        put("username", newUser.username)
                        put("display_name", newUser.displayName)
                        put("profile_picture", "")
                        put("role", newUser.role.value)
                        put("status", true)

                    }
                }

                Result.success(Unit)
            } catch (exception: Exception) {
                Log.e(TAG, "signUp:failure - ${exception.message}", exception)
                Result.failure(exception)
            }
        }

    suspend fun signIn(context: Context, email: String, password: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }

                val currentUser = supabase.auth.currentUserOrNull()
                    ?: throw Exception("Authentication succeeded but user data is unavailable")

                val isActive: Boolean? = currentUser
                    .userMetadata
                    ?.get("status")
                    ?.jsonPrimitive
                    ?.booleanOrNull

                if (isActive != null && !isActive) {
                    supabase.auth.signOut()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Account is inactive. Please contact support.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    return@withContext Result.failure(Exception("account_inactive"))
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Signed in successfully!", Toast.LENGTH_SHORT).show()
                }

                Result.success(Unit)
            } catch (exception: Exception) {
                Log.e(TAG, "signInWithEmail:failure - ${exception.message}", exception)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Sign-in failed: ${exception.message ?: "Unknown error"}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                Result.failure(exception)
            }
        }

    suspend fun updateEmail(currentPassword: String, newEmail: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val currentUser = supabase.auth.currentUserOrNull()
                    ?: return@withContext Result.failure(Exception("No authenticated user"))

                val currentEmail = currentUser.email!!

                Log.d(TAG, "Current email: $currentEmail, New email: $newEmail")

                supabase.auth.signInWith(Email) {
                    this.email = currentEmail
                    this.password = currentPassword
                }

                UserService.updateUserInfo(
                    UpdateUserDTO(
                        id = currentUser.id,
                        email = newEmail,
                    )
                )

                Result.success(Unit)
            } catch (exception: Exception) {
                Log.e(TAG, "updateEmail:failure - ${exception.message}", exception)
                Result.failure(exception)
            }
        }

    suspend fun updatePassword(currentPassword: String, newPassword: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val currentUser = supabase.auth.currentUserOrNull()
                    ?: return@withContext Result.failure(Exception("No authenticated user"))

                val currentEmail = currentUser.email!!

                supabase.auth.signInWith(Email) {
                    this.email = currentEmail
                    this.password = currentPassword
                }

                UserService.updateUserInfo(
                    UpdateUserDTO(
                        id = currentUser.id,
                        password = newPassword,
                    )
                )

                Result.success(Unit)
            } catch (exception: Exception) {
                Log.e(TAG, "updatePassword:failure - ${exception.message}", exception)
                Result.failure(exception)
            }
        }
}