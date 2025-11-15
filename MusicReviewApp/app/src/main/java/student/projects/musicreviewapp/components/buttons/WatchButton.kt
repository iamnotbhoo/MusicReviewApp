package student.projects.musicreviewapp.components.buttons

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import student.projects.musicreviewapp.R

class ListenButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private var isListened: Boolean = false
    private var musicId: String = ""
    private var musicTitle: String = ""

    private lateinit var listenIcon: ImageView

    var onListenChange: ((Boolean) -> Unit)? = null
    var onEvent: (() -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_listen_button, this, true)
        listenIcon = findViewById(R.id.listen_icon)

        setOnClickListener {
            onListen()
        }

        updateIcon()
    }

    fun setMusicData(id: String, title: String, listened: Boolean) {
        musicId = id
        musicTitle = title
        isListened = listened
        updateIcon()
    }

    private fun onListen() {
        onEvent?.invoke()

        if (isListened) {
            removeFromListened()
        } else {
            addToListened()
        }
    }

    private fun addToListened() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            showError("Please sign in to mark as listened")
            return
        }

        val userRef = db.collection("users").document(currentUser.uid)
        userRef.update("listened", com.google.firebase.firestore.FieldValue.arrayUnion(musicId))
            .addOnSuccessListener {
                isListened = true
                updateIcon()
                onListenChange?.invoke(true)
                showSuccess("Marked as listened")
            }
            .addOnFailureListener { e ->
                showError("Failed to mark as listened: ${e.message}")
            }
    }

    private fun removeFromListened() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            showError("Please sign in to remove from listened")
            return
        }

        val userRef = db.collection("users").document(currentUser.uid)
        userRef.update("listened", com.google.firebase.firestore.FieldValue.arrayRemove(musicId))
            .addOnSuccessListener {
                isListened = false
                updateIcon()
                onListenChange?.invoke(false)
                showSuccess("Removed from listened")
            }
            .addOnFailureListener { e ->
                showError("Failed to remove from listened: ${e.message}")
            }
    }

    private fun updateIcon() {
        val iconRes = if (isListened) {
            R.drawable.ic_listened_filled
        } else {
            R.drawable.ic_listened_border
        }
        listenIcon.setImageResource(iconRes)
    }

    private fun showSuccess(message: String) {
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}