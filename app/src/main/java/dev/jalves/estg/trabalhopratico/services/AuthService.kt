package dev.jalves.estg.trabalhopratico.services

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Toast
import dev.jalves.estg.trabalhopratico.dto.CreateUserDTO
import dev.jalves.estg.trabalhopratico.objects.TaskSyncUser
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AuthService {
    suspend fun signUp(context: Context, newUser: CreateUserDTO): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val authResult = supabase.auth.signUpWith(Email) {
                    this.email = newUser.email
                    password = newUser.password

                }

                val userId = authResult?.id ?: run {
                    val errorMsg = "User creation failed - no user ID returned"
                    Log.e(TAG, errorMsg)
                    return@withContext Result.failure(Exception(errorMsg))
                }

                Log.d(TAG, "Auth user created successfully - UserID: $userId")

                val existingUsers = supabase.from("users")
                    .select {
                        filter {
                            eq("uid", userId)
                        }
                    }
                    .decodeList<TaskSyncUser>()

                if (existingUsers.isEmpty()) {
                    val userDoc = TaskSyncUser(
                        uid = userId,
                        displayName = newUser.name,
                        username = newUser.username,
                        profilePicture = "",
                        role = "user"
                    )

                    supabase.from("users").insert(userDoc)
                    Log.d(TAG, "User profile created successfully")
                } else {
                    Log.d(TAG, "User profile already exists, skipping insert")
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Account created successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                Result.success(Unit)

            } catch (exception: Exception) {
                Log.e(TAG, "Authentication failed", exception)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Error: ${exception.message ?: "Unknown error"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                Result.failure(exception)
            }
        }

    suspend fun signIn(context: Context, email: String, password: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val result = supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
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
}