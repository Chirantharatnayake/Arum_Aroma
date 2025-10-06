package com.example.loginpage.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.EmailAuthProvider

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
                        // NOTE: Storing plaintext passwords is insecure; kept to match existing schema.
                        "password" to password,
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

    /**
     * Update one or more fields for the currently signed-in user.
     * Any parameter may be null to skip updating that field.
     * - newUsername updates Firestore `users/{uid}.username`
     * - newEmail updates FirebaseAuth email and Firestore `users/{uid}.email`
     * - newPassword updates FirebaseAuth password (requires recent login)
     * - currentPassword is required when changing email or password
     */
    fun updateUserProfile(
        newUsername: String?,
        newEmail: String?,
        newPassword: String?,
        currentPassword: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onError("Not logged in")
            return
        }
        val uid = user.uid

        fun updateFirestore(finalEmail: String?) {
            val updates = mutableMapOf<String, Any>()
            if (!newUsername.isNullOrBlank()) updates["username"] = newUsername
            if (!finalEmail.isNullOrBlank()) updates["email"] = finalEmail

            if (updates.isEmpty()) {
                onSuccess()
                return
            }
            firestore.collection("users").document(uid)
                .update(updates as Map<String, Any>)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onError("Firestore update failed: ${e.message}") }
        }

        fun updatePasswordThenFirestore(currentEmailForFirestore: String?) {
            if (newPassword.isNullOrBlank()) {
                updateFirestore(currentEmailForFirestore)
                return
            }
            user.updatePassword(newPassword)
                .addOnSuccessListener {
                    updateFirestore(currentEmailForFirestore)
                }
                .addOnFailureListener { e ->
                    val msg = e.message ?: "Failed to update password"
                    onError(msg)
                }
        }

        fun updateEmailThenPassword() {
            if (newEmail.isNullOrBlank() || newEmail == user.email) {
                updatePasswordThenFirestore(user.email)
                return
            }
            user.updateEmail(newEmail)
                .addOnSuccessListener {
                    updatePasswordThenFirestore(newEmail)
                }
                .addOnFailureListener { e ->
                    val msg = e.message ?: "Failed to update email"
                    onError(msg)
                }
        }

        // Determine if sensitive changes require reauthentication
        val wantsEmailChange = !newEmail.isNullOrBlank() && newEmail != user.email
        val wantsPassChange = !newPassword.isNullOrBlank()
        val needsReauth = wantsEmailChange || wantsPassChange

        if (needsReauth) {
            val currentEmail = user.email
            if (currentEmail.isNullOrBlank()) {
                onError("Cannot reauthenticate: no email on record")
                return
            }
            if (currentPassword.isNullOrBlank()) {
                onError("Please enter your current password to update email or password.")
                return
            }
            val cred = EmailAuthProvider.getCredential(currentEmail, currentPassword)
            user.reauthenticate(cred)
                .addOnSuccessListener { updateEmailThenPassword() }
                .addOnFailureListener { e -> onError("Reauthentication failed: ${e.message}") }
        } else {
            // No sensitive updates; just Firestore mirroring
            updateFirestore(user.email)
        }
    }
}
