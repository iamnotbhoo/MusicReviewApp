package student.projects.musicreviewapp.components.music

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import student.projects.musicreviewapp.R

class MusicReviews @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var reviewsRecyclerView: RecyclerView
    private lateinit var noReviewsText: TextView
    private lateinit var reviewInput: EditText
    private lateinit var submitButton: TextView

    private val reviewsAdapter = MusicReviewAdapter(emptyList())

    var onSubmitReview: ((String) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_music_reviews, this, true)
        setupViews()
    }

    private fun setupViews() {
        reviewsRecyclerView = findViewById(R.id.reviews_recycler_view)
        noReviewsText = findViewById(R.id.no_reviews_text)
        reviewInput = findViewById(R.id.review_input)
        submitButton = findViewById(R.id.submit_review_button)

        reviewsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reviewsAdapter
        }

        submitButton.setOnClickListener {
            val reviewText = reviewInput.text.toString().trim()
            if (reviewText.isNotEmpty()) {
                onSubmitReview?.invoke(reviewText)
                reviewInput.text.clear()
            }
        }
    }

    fun setReviews(reviews: List<MusicReview>) {
        if (reviews.isEmpty()) {
            noReviewsText.visibility = View.VISIBLE
            reviewsRecyclerView.visibility = View.GONE
        } else {
            noReviewsText.visibility = View.GONE
            reviewsRecyclerView.visibility = View.VISIBLE
            reviewsAdapter.updateData(reviews)
        }
    }
}

// Separate data class to avoid conflicts
data class MusicReview(
    val id: String,
    val userId: String,
    val userName: String,
    val userPhotoUrl: String?,
    val content: String,
    val timestamp: String
)

class MusicReviewAdapter(private var reviews: List<MusicReview>) :
    RecyclerView.Adapter<MusicReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Update these to match the actual IDs in layout_music_review_item.xml
        val userName: TextView = itemView.findViewById(R.id.user_name) // Changed from review_user_name
        val content: TextView = itemView.findViewById(R.id.review_content)
        val timestamp: TextView = itemView.findViewById(R.id.review_timestamp)
        // You can also add other views if needed
        val albumTitle: TextView = itemView.findViewById(R.id.album_title)
        val likeCount: TextView = itemView.findViewById(R.id.like_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_music_review_item, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.userName.text = review.userName
        holder.content.text = review.content
        holder.timestamp.text = review.timestamp
    }

    override fun getItemCount(): Int = reviews.size

    fun updateData(newReviews: List<MusicReview>) {
        reviews = newReviews
        notifyDataSetChanged()
    }
}