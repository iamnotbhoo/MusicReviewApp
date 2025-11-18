package student.projects.musicreviewapp.auth

import com.google.firebase.auth.FirebaseAuth

class AuthManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun signUp(email: String, password: String, username: String, onResult: (Boolean, String?) -> Unit) {
        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            onResult(false, "Email, password, or username cannot be empty")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun signIn(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        if (email.isEmpty() || password.isEmpty()) {
            onResult(false, "Email or password cannot be empty")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUser(): String? {
        return auth.currentUser?.displayName
    }

    fun getCurrentEmail(): String? {
        return auth.currentUser?.email
    }

    fun getCurrentUid(): String? {
        return auth.currentUser?.uid
    }

    fun logout() {
        auth.signOut()
    }
}
