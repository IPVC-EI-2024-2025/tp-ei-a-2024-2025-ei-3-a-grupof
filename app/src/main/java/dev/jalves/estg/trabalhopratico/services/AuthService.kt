package dev.jalves.estg.trabalhopratico.services

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import dev.jalves.estg.trabalhopratico.dto.CreateUserDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

object AuthService {
    private val auth = Firebase.auth

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun signUp(context: Context, newUser: CreateUserDTO): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            val task = auth.createUserWithEmailAndPassword(newUser.email, newUser.password)
                .addOnSuccessListener {
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser

                    user!!.updateProfile(userProfileChangeRequest {
                        displayName = newUser.name
                    }).addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "Success: " + user.email,
                            Toast.LENGTH_SHORT,
                        ).show()

                        continuation.resume(Result.success(user), onCancellation = {})
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "createUserWithEmail:failure", exception)
                    Toast.makeText(
                        context,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()

                    continuation.resume(Result.failure(exception), onCancellation = {})
                }

            continuation.invokeOnCancellation {
                task.addOnCompleteListener {}
            }
        }
    }

    fun signIn(context: Context, email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Log.d(TAG, "signInWithEmail:success")
                val user = auth.currentUser
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "signInWithEmail:failure", exception)
                Toast.makeText(
                    context,
                    "Authentication failed.",
                    Toast.LENGTH_SHORT,
                ).show()
            }
    }
}