package student.projects.musicreviewapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.models.Review

class ReviewsAdapter(
    private val onReviewClick: (Review) -> Unit,
    private val onLikeClick: (Review, Boolean) -> Unit
) : ListAdapter<Review, ReviewsAdapter.ReviewViewHolder>(ReviewDiffCallback()) {

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
        val likesCount: TextView = itemView.findViewById(R.id.likes_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = getItem(position)

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
        holder.likesCount.text = "${review.likes}"

        // Update star ratings
        updateStarRating(holder, review.rating)

        // Update like icon
        updateLikeIcon(holder, review.liked)

        // Set click listener for the entire review item
        holder.itemView.setOnClickListener {
            onReviewClick(review)
        }

        // Set like click listener
        holder.likeIcon.setOnClickListener {
            val newLiked = !review.liked
            onLikeClick(review, newLiked)
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
        if (liked) {
            holder.likeIcon.setImageResource(R.drawable.ic_heart_orange)
            holder.likeIcon.setColorFilter(
                ContextCompat.getColor(holder.itemView.context, R.color.orange_500)
            )
        } else {
            holder.likeIcon.setImageResource(R.drawable.ic_heart)
            holder.likeIcon.setColorFilter(
                ContextCompat.getColor(holder.itemView.context, R.color.gray_400)
            )
        }
    }
}

class ReviewDiffCallback : DiffUtil.ItemCallback<Review>() {
    override fun areItemsTheSame(oldItem: Review, newItem: Review): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Review, newItem: Review): Boolean {
        return oldItem == newItem
    }
}