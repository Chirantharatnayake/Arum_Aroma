package com.example.loginpage.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun signOut() {
        auth.signOut()
    }

    fun registerUser(
        username: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val userData = hashMapOf(
                        "username" to username,
                        "email" to email,
                        "password" to password, // optional: remove this for security
                        "uid" to userId
                    )
                    firestore.collection("users").document(userId).set(userData)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e -> onError("Firestore error: ${e.message}") }
                } else {
                    onError(task.exception?.message ?: "Registration failed")
                }
            }
    }

    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(task.exception?.message ?: "Login failed")
                }
            }
    }

    /** Load the current signed-in user's profile from Firestore (username/email). */
    fun loadCurrentUserProfile(onLoaded: (username: String?, email: String?) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onLoaded(null, null)
            return
        }
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val username = doc.getString("username")
                val email = doc.getString("email")
                onLoaded(username, email)
            }
            .addOnFailureListener { e ->
                Log.w("FirebaseManager", "Failed to load user profile: ${e.message}")
                onLoaded(null, null)
            }
    }
}
