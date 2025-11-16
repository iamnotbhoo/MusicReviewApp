package student.projects.musicreviewapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.models.Review

class ReviewsAdapter(private var reviews: List<Review>) :
    RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>() {

    // ADDED: Click listener property
    private var onReviewClickListener: ((Review) -> Unit)? = null

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val albumCover: ImageView = itemView.findViewById(R.id.album_cover)
        val albumTitle: TextView = itemView.findViewById(R.id.album_title)
        val albumYear: TextView = itemView.findViewById(R.id.album_year)
        val reviewText: TextView = itemView.findViewById(R.id.review_text)
        val userName: TextView = itemView.findViewById(R.id.user_name)
        val star1: ImageView = itemView.findViewById(R.id.star1)
        val star2: ImageView = itemView.findViewById(R.id.star2)
        val star3: ImageView = itemView.findViewById(R.id.star3)
        val star4: ImageView = itemView.findViewById(R.id.star4)
        val star5: ImageView = itemView.findViewById(R.id.star5)
        val likeIcon: ImageView = itemView.findViewById(R.id.like_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]

        // Load album cover
        if (!review.musicCoverUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(review.musicCoverUrl)
                .placeholder(R.drawable.album_placeholder)
                .error(R.drawable.album_placeholder)
                .into(holder.albumCover)
        } else {
            holder.albumCover.setImageResource(R.drawable.album_placeholder)
        }

        // Set text content
        holder.albumTitle.text = review.musicTitle
        holder.albumYear.text = review.musicYear
        holder.reviewText.text = review.content
        holder.userName.text = review.userName

        // Update star ratings
        updateStarRating(holder, review.rating)

        // Update like icon
        updateLikeIcon(holder, review.liked)

        // ADDED: Set click listener for the entire review item
        holder.itemView.setOnClickListener {
            onReviewClickListener?.invoke(review)
        }

        // ADDED: Set like click listener
        holder.likeIcon.setOnClickListener {
            // Toggle like state (you can update this in your database)
            val newLiked = !review.liked
            updateLikeIcon(holder, newLiked)
        }
    }

    private fun updateStarRating(holder: ReviewViewHolder, rating: Int) {
        val stars = listOf(holder.star1, holder.star2, holder.star3, holder.star4, holder.star5)
        val activeColor = ContextCompat.getColor(holder.itemView.context, R.color.purple_500)
        val inactiveColor = ContextCompat.getColor(holder.itemView.context, R.color.gray_400)

        stars.forEachIndexed { index, star ->
            val color = if (index < rating) activeColor else inactiveColor
            ImageViewCompat.setImageTintList(star, android.content.res.ColorStateList.valueOf(color))
        }
    }

    private fun updateLikeIcon(holder: ReviewViewHolder, liked: Boolean) {
        val color = if (liked)
            ContextCompat.getColor(holder.itemView.context, R.color.orange_500)
        else
            ContextCompat.getColor(holder.itemView.context, R.color.gray_400)

        ImageViewCompat.setImageTintList(holder.likeIcon, android.content.res.ColorStateList.valueOf(color))
    }

    override fun getItemCount(): Int = reviews.size

    fun updateData(newReviews: List<Review>) {
        reviews = newReviews
        notifyDataSetChanged()
    }

    // ADDED: Method to set click listener
    fun setOnReviewClickListener(listener: (Review) -> Unit) {
        onReviewClickListener = listener
    }
}