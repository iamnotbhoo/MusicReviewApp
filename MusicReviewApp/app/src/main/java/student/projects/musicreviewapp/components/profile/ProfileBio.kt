package student.projects.musicreviewapp.components.profile

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.models.User

class ProfileBio @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var profileImage: ImageView
    private lateinit var userName: TextView
    private lateinit var userBio: TextView
    private lateinit var editProfileButton: TextView
    private lateinit var favoritesCount: TextView
    private lateinit var listenedCount: TextView

    var onEditProfile: (() -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_profile_bio, this, true)
        setupViews()
    }

    private fun setupViews() {
        profileImage = findViewById(R.id.profile_image)
        userName = findViewById(R.id.user_name)
        userBio = findViewById(R.id.user_bio)
        editProfileButton = findViewById(R.id.edit_profile_button)
        favoritesCount = findViewById(R.id.favorites_count)
        listenedCount = findViewById(R.id.listened_count)

        editProfileButton.setOnClickListener {
            onEditProfile?.invoke()
        }
    }

    fun setUserData(user: User, isAuthor: Boolean) {
        // Load profile image
        if (!user.profilePicture.isNullOrEmpty()) {
            Glide.with(context)
                .load(user.profilePicture)
                .placeholder(R.drawable.placeholder_profile)
                .into(profileImage)
        } else {
            profileImage.setImageResource(R.drawable.placeholder_profile)
        }

        userName.text = user.username
        userBio.text = user.bio ?: "No bio yet"

        // Show edit button only if user is viewing their own profile
        editProfileButton.visibility = if (isAuthor) android.view.View.VISIBLE else android.view.View.GONE

        // Set counts (you'll need to fetch these from Firestore)
        favoritesCount.text = "0" // Replace with actual count
        listenedCount.text = "0"  // Replace with actual count
    }

    fun updateCounts(favorites: Int, listened: Int) {
        favoritesCount.text = favorites.toString()
        listenedCount.text = listened.toString()
    }
}