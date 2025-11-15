package student.projects.musicreviewapp.auth

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import student.projects.musicreviewapp.R

class SignInWithGoogle @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var signInLauncher: ActivityResultLauncher<android.content.Intent>

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_sign_in_google, this, true)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("YOUR_WEB_CLIENT_ID") // You need to get this from Firebase console
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)

        val googleButton = findViewById<MaterialButton>(R.id.google_button)
        googleButton.setOnClickListener {
            signInWithGoogle()
        }
    }

    fun setSignInLauncher(launcher: ActivityResultLauncher<android.content.Intent>) {
        signInLauncher = launcher
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    fun handleSignInResult(data: android.content.Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            println("Google sign in failed: ${e.statusCode}")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Check if user exists in Firestore
                    checkAndCreateUserInFirestore()
                } else {
                    println("Firebase authentication failed: ${task.exception?.message}")
                }
            }
    }

    private fun checkAndCreateUserInFirestore() {
        val currentUser = auth.currentUser
        if (currentUser == null) return

        val userRef = db.collection("users").document(currentUser.uid)
        userRef.get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    addNewUserToDB(currentUser.uid)
                }
            }
    }

    private fun addNewUserToDB(uid: String) {
        val currentUser = auth.currentUser ?: return

        val userData = hashMapOf(
            "name" to currentUser.displayName,
            "uid" to uid,
            "bio" to "My music listening journey, on MusicReviewApp :)",
            "photoUrl" to currentUser.photoUrl?.toString(),
            "reviews" to emptyList<String>(),
            "listened" to emptyList<String>(),
            "favourites" to emptyList<String>()
        )

        db.collection("users").document(uid)
            .set(userData)
            .addOnSuccessListener {
                println("User document created successfully")
            }
            .addOnFailureListener { e ->
                println("Error creating user document: ${e.message}")
            }
    }
}