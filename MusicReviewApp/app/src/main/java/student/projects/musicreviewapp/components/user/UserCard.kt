package student.projects.musicreviewapp.components.user

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.models.User

class UserCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var userAvatar: ImageView
    private lateinit var userName: TextView
    private lateinit var favoritesCount: TextView
    private lateinit var listenedCount: TextView

    var onUserClick: ((String) -> Unit)? = null

    private var currentUserId = ""

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_user_card, this, true)
        setupViews()
    }

    private fun setupViews() {
        userAvatar = findViewById(R.id.user_card_avatar)
        userName = findViewById(R.id.user_card_name)
        favoritesCount = findViewById(R.id.user_card_favorites_count)
        listenedCount = findViewById(R.id.user_card_listened_count)

        // Set click listeners
        userAvatar.setOnClickListener {
            onUserClick?.invoke(currentUserId)
        }

        userName.setOnClickListener {
            onUserClick?.invoke(currentUserId)
        }

        setOnClickListener {
            onUserClick?.invoke(currentUserId)
        }
    }

    fun setUser(user: User) {
        currentUserId = user.id

        // Load user avatar
        if (!user.profilePicture.isNullOrEmpty()) {
            Glide.with(context)
                .load(user.profilePicture)
                .placeholder(R.drawable.placeholder_profile)
                .into(userAvatar)
        } else {
            userAvatar.setImageResource(R.drawable.placeholder_profile)
        }

        userName.text = user.username

        // Set counts (you'll need to fetch these from your data)
        favoritesCount.text = "0" // Replace with user.favourites?.size ?: 0
        listenedCount.text = "0"  // Replace with user.listened?.size ?: 0
    }

    fun updateCounts(favorites: Int, listened: Int) {
        favoritesCount.text = favorites.toString()
        listenedCount.text = listened.toString()
    }
}