package student.projects.musicreviewapp.components.profile

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.models.Review

class ProfileReviews @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var noReviewsText: TextView

    private val adapter = ProfileReviewsAdapter(emptyList())

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_profile_reviews, this, true)
        setupViews()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.profile_reviews_recycler_view)
        noReviewsText = findViewById(R.id.no_profile_reviews_text)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ProfileReviews.adapter
        }
    }

    fun setReviews(reviews: List<Review>) {
        if (reviews.isEmpty()) {
            noReviewsText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            noReviewsText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.updateData(reviews)
        }
    }
}

class ProfileReviewsAdapter(private var reviews: List<Review>) :
    RecyclerView.Adapter<ProfileReviewsAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Update to match the actual IDs in layout_music_review_item.xml
        val userName: TextView = itemView.findViewById(R.id.user_name) // Changed from review_user_name
        val content: TextView = itemView.findViewById(R.id.review_content)
        val timestamp: TextView = itemView.findViewById(R.id.review_timestamp)
        // You can also add other views if you're using them
        val albumTitle: TextView = itemView.findViewById(R.id.album_title)
        val albumYear: TextView = itemView.findViewById(R.id.album_year)
        val likeCount: TextView = itemView.findViewById(R.id.like_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.userName.text = review.userName
        holder.content.text = review.content
        holder.timestamp.text = review.timestamp
    }

    override fun getItemCount(): Int = reviews.size

    fun updateData(newReviews: List<Review>) {
        reviews = newReviews
        notifyDataSetChanged()
    }
}