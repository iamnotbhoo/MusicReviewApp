package student.projects.musicreviewapp.components.review

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.models.Review as ReviewModel

class MusicReviewCompact @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var userAvatar: ImageView
    private lateinit var userName: TextView
    private lateinit var timestamp: TextView
    private lateinit var reviewContent: TextView
    private lateinit var deleteButton: TextView

    private val auth = Firebase.auth

    var onUserClick: ((String) -> Unit)? = null
    var onDelete: ((ReviewModel) -> Unit)? = null

    private var currentReview: ReviewModel? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_music_review_compact, this, true)
        setupViews()
    }

    private fun setupViews() {
        userAvatar = findViewById(R.id.compact_user_avatar)
        userName = findViewById(R.id.compact_user_name)
        timestamp = findViewById(R.id.compact_timestamp)
        reviewContent = findViewById(R.id.compact_review_content)
        deleteButton = findViewById(R.id.compact_delete_button)

        userAvatar.setOnClickListener {
            currentReview?.userId?.let { userId ->
                onUserClick?.invoke(userId)
            }
        }

        userName.setOnClickListener {
            currentReview?.userId?.let { userId ->
                onUserClick?.invoke(userId)
            }
        }

        deleteButton.setOnClickListener {
            currentReview?.let { review ->
                onDelete?.invoke(review)
            }
        }
    }

    fun setReview(review: ReviewModel) {
        currentReview = review

        // Load user avatar
        if (!review.userPhotoUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(review.userPhotoUrl)
                .placeholder(R.drawable.placeholder_profile)
                .into(userAvatar)
        } else {
            userAvatar.setImageResource(R.drawable.placeholder_profile)
        }

        userName.text = review.userName
        timestamp.text = review.timestamp
        reviewContent.text = review.content

        // Show delete button only if current user is the author
        val isAuthor = auth.currentUser?.uid == review.userId
        deleteButton.visibility = if (isAuthor && onDelete != null) View.VISIBLE else View.GONE
    }
}