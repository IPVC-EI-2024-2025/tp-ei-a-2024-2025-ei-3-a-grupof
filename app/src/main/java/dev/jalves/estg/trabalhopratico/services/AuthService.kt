package dev.jalves.estg.trabalhopratico.services

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import dev.jalves.estg.trabalhopratico.dto.CreateUserDTO
import dev.jalves.estg.trabalhopratico.objects.TaskSyncUser
import dev.jalves.estg.trabalhopratico.services.SupabaseService.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

object AuthService {
    private val auth = Firebase.auth

    suspend fun signUp(newUser: CreateUserDTO) = withContext(Dispatchers.IO) {
        val user = supabase.auth.signUpWith(Email) {
            email = newUser.email
            password = newUser.password
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun signIn(context: Context, email: String, password: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser!!

                    continuation.resume(Result.success(user), onCancellation = {})
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "signInWithEmail:failure", exception)
                    Toast.makeText(
                        context,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()

                    continuation.resume(Result.failure(exception), onCancellation = {})
                }
        }
    }

    fun fetchCurrentUserProfile(onResult: (TaskSyncUser?) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val user = doc.toObject(TaskSyncUser::class.java)
                onResult(user)
            }
    }
}