package dev.jalves.estg.trabalhopratico.services

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Toast
import dev.jalves.estg.trabalhopratico.dto.CreateUserDTO
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object AuthService {
    suspend fun signUp(newUser: CreateUserDTO): Result<Unit> =
        withContext(Dispatchers.IO) {
            supabase.auth.signUpWith(Email) {
                email = newUser.email
                password = newUser.password
                data = buildJsonObject {
                    put("username", newUser.username)
                    put("display_name", newUser.name)
                    put("profile_picture", "")
                }
            }

            Result.success(Unit)
        }

    suspend fun signIn(context: Context, email: String, password: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                supabase.auth.signInWith(Email) {
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