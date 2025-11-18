package student.projects.musicreviewapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.models.Review
import student.projects.musicreviewapp.repositories.FirebaseRepository

class UserReviewsFragment : Fragment() {

    private lateinit var reviewsAdapter: UserReviewsAdapter
    private val repository = FirebaseRepository()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId get() = auth.currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_reviews, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        loadUserReviews()
    }

    private fun setupViews(view: View) {
        // Setup back button
        view.findViewById<ImageView>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }

        // Setup RecyclerView
        val reviewsRecycler = view.findViewById<RecyclerView>(R.id.reviews_recycler)
        reviewsAdapter = UserReviewsAdapter { review ->
            toggleReviewLike(review)
        }

        // Set click listener for reviews
        reviewsAdapter.onReviewClick = { review ->
            navigateToReviewDetail(review)
        }

        reviewsRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reviewsAdapter
        }

        // Setup empty state
        updateEmptyState()
    }

    private fun navigateToReviewDetail(review: Review) {
        val bundle = Bundle().apply {
            putParcelable("review", review)
        }
        findNavController().navigate(R.id.action_userReviewsFragment_to_reviewDetailFragment, bundle)
    }

    private fun loadUserReviews() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val reviews = repository.getReviewsByUser(currentUserId)
                reviewsAdapter.submitList(reviews)
                updateEmptyState()
            } catch (e: Exception) {
                showToast("Failed to load your reviews")
            }
        }
    }

    private fun toggleReviewLike(review: Review) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val isCurrentlyLiked = repository.isReviewLiked(currentUserId, review.id)
                if (isCurrentlyLiked) {
                    repository.unlikeReview(currentUserId, review.id)
                    // Update local review object
                    val updatedReview = review.copy(
                        liked = false,
                        likes = maxOf(0, review.likes - 1)
                    )
                    updateReviewInAdapter(updatedReview)
                    showToast("Review unliked")
                } else {
                    repository.likeReview(currentUserId, review.id)
                    // Update local review object
                    val updatedReview = review.copy(
                        liked = true,
                        likes = review.likes + 1
                    )
                    updateReviewInAdapter(updatedReview)
                    showToast("Review liked")
                }
            } catch (e: Exception) {
                showToast("Failed to update like")
            }
        }
    }

    private fun updateReviewInAdapter(updatedReview: Review) {
        val currentList = reviewsAdapter.currentList.toMutableList()
        val index = currentList.indexOfFirst { it.id == updatedReview.id }
        if (index != -1) {
            currentList[index] = updatedReview
            reviewsAdapter.submitList(currentList)
        }
    }

    private fun updateEmptyState() {
        val emptyState = requireView().findViewById<View>(R.id.empty_state)
        val reviewsRecycler = requireView().findViewById<RecyclerView>(R.id.reviews_recycler)

        if (reviewsAdapter.itemCount == 0) {
            emptyState?.visibility = View.VISIBLE
            reviewsRecycler?.visibility = View.GONE
        } else {
            emptyState?.visibility = View.GONE
            reviewsRecycler?.visibility = View.VISIBLE
        }
    }

    class UserReviewsAdapter(
        private val onLikeClick: (Review) -> Unit
    ) : ListAdapter<Review, UserReviewsAdapter.ReviewViewHolder>(ReviewDiffCallback()) {

        var onReviewClick: ((Review) -> Unit)? = null

        class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val albumCover: ImageView = itemView.findViewById(R.id.album_cover)
            val albumTitle: TextView = itemView.findViewById(R.id.album_title)
            val albumYear: TextView = itemView.findViewById(R.id.album_year)
            val reviewContent: TextView = itemView.findViewById(R.id.review_content)
            val timestamp: TextView = itemView.findViewById(R.id.review_timestamp)
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
                .inflate(R.layout.item_user_review, parent, false)
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
            holder.reviewContent.text = review.content
            holder.timestamp.text = formatTimestamp(review.timestamp)
            holder.likesCount.text = "${review.likes}"

            // Update star ratings
            updateStarRating(holder, review.rating)

            // Update like icon
            updateLikeIcon(holder, review.liked)

            // Set like click listener
            holder.likeIcon.setOnClickListener {
                onLikeClick(review)
            }

            // Set click listener for the entire review item
            holder.itemView.setOnClickListener {
                onReviewClick?.invoke(review)
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

        private fun formatTimestamp(timestamp: String): String {
            // Format the timestamp for better display
            return try {
                // You can implement your timestamp formatting logic here
                // For example: convert ISO format to "2 days ago", "1 week ago", etc.
                timestamp
            } catch (e: Exception) {
                timestamp
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

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this fragment
        loadUserReviews()
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }
}