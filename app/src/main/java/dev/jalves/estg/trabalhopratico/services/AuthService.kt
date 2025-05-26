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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

object AuthService {
    private val auth = Firebase.auth

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun signUp(context: Context, newUser: CreateUserDTO): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            auth.createUserWithEmailAndPassword(newUser.email, newUser.password)
                .addOnSuccessListener { authResult ->
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser

                    val uid = authResult.user?.uid ?: return@addOnSuccessListener
                    val userDoc = TaskSyncUser(
                        uid = uid,
                        email = newUser.email,
                        displayName = newUser.name,
                        username = newUser.username,
                        profilePicture = "",
                        role = "user"
                    )
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .set(userDoc)
                        .addOnSuccessListener {
                            Toast.makeText(
                                context,
                                "Success: " + userDoc.email,
                                Toast.LENGTH_SHORT,
                            ).show()

                            continuation.resume(Result.success(user!!), onCancellation = {})
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