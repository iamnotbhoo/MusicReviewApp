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

class FavouriteButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private var isFavourite: Boolean = false
    private var musicId: String = ""
    private var musicTitle: String = ""

    private lateinit var favouriteIcon: ImageView

    var onFavouriteChange: ((Boolean) -> Unit)? = null
    var onEvent: (() -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_favourite_button, this, true)
        favouriteIcon = findViewById(R.id.favourite_icon)

        setOnClickListener {
            onFavourite()
        }

        updateIcon()
    }

    fun setMusicData(id: String, title: String, isFav: Boolean) {
        musicId = id
        musicTitle = title
        isFavourite = isFav
        updateIcon()
    }

    private fun onFavourite() {
        onEvent?.invoke()

        if (isFavourite) {
            removeFromFavourites()
        } else {
            addToFavourites()
        }
    }

    private fun addToFavourites() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            showError("Please sign in to add favourites")
            return
        }

        val userRef = db.collection("users").document(currentUser.uid)
        userRef.update("favourites", com.google.firebase.firestore.FieldValue.arrayUnion(musicId))
            .addOnSuccessListener {
                isFavourite = true
                updateIcon()
                onFavouriteChange?.invoke(true)
                showSuccess("Added to favourites")
            }
            .addOnFailureListener { e ->
                showError("Failed to add to favourites: ${e.message}")
            }
    }

    private fun removeFromFavourites() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            showError("Please sign in to remove favourites")
            return
        }

        val userRef = db.collection("users").document(currentUser.uid)
        userRef.update("favourites", com.google.firebase.firestore.FieldValue.arrayRemove(musicId))
            .addOnSuccessListener {
                isFavourite = false
                updateIcon()
                onFavouriteChange?.invoke(false)
                showSuccess("Removed from favourites")
            }
            .addOnFailureListener { e ->
                showError("Failed to remove from favourites: ${e.message}")
            }
    }

    private fun updateIcon() {
        val iconRes = if (isFavourite) {
            R.drawable.ic_favorite_filled
        } else {
            R.drawable.ic_favorite_border
        }
        favouriteIcon.setImageResource(iconRes)
    }

    private fun showSuccess(message: String) {
        // You can implement Toast or Snackbar here
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}