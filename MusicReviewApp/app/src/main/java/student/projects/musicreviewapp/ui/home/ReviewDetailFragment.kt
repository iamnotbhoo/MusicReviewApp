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
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.auth.FirebaseLikeManager
import student.projects.musicreviewapp.auth.FirebaseReviewManager
import student.projects.musicreviewapp.models.Review

class ReviewDetailFragment : Fragment() {

    private lateinit var review: Review
    private lateinit var likeManager: FirebaseLikeManager
    private lateinit var reviewManager: FirebaseReviewManager
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId get() = auth.currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        likeManager = FirebaseLikeManager(requireContext())
        reviewManager = FirebaseReviewManager(requireContext())
        return inflater.inflate(R.layout.fragment_review_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            review = bundle.getParcelable("review") ?: return@let
        }

        setupViews(view)
        setupBackButton(view)
        setupLikeButton(view)
    }

    private fun setupViews(view: View) {
        // Set user info
        view.findViewById<TextView>(R.id.user_name).text = review.userName

        // Load user profile image if available
        val userPhoto = view.findViewById<ImageView>(R.id.user_photo)
        if (!review.userPhotoUrl.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(review.userPhotoUrl)
                .placeholder(R.drawable.placeholder_profile)
                .error(R.drawable.placeholder_profile)
                .circleCrop()
                .into(userPhoto)
        } else {
            userPhoto.setImageResource(R.drawable.placeholder_profile)
        }

        val listenStatus = if (review.isFirstListen) "First Listen" else "Listened before"
        view.findViewById<TextView>(R.id.review_date).text = "$listenStatus • ${review.timestamp}"

        // Set album info
        view.findViewById<TextView>(R.id.album_title).text = review.musicTitle
        view.findViewById<TextView>(R.id.album_year).text = review.musicYear

        // Set review content
        view.findViewById<TextView>(R.id.review_content).text = review.content

        // Set timestamp
        view.findViewById<TextView>(R.id.review_date).text = "Listened ${review.timestamp}"

        // Set likes count - use actual likes from review
        view.findViewById<TextView>(R.id.likes_count).text = "${review.likes} likes"

        // Set star rating
        updateStarRating(view, review.rating)

        // Setup bottom actions
        setupBottomActions(view)
    }

    private fun setupBackButton(view: View) {
        view.findViewById<ImageView>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupLikeButton(view: View) {
        val likeButton = view.findViewById<ImageView>(R.id.like_icon)
        val likeCountText = view.findViewById<TextView>(R.id.likes_count)

        // Set initial state using callback
        likeManager.isReviewLiked(review.id) { isLiked ->
            activity?.runOnUiThread {
                updateLikeButton(isLiked, review.likes, likeButton, likeCountText)
            }
        }

        likeButton.setOnClickListener {
            likeManager.isReviewLiked(review.id) { isCurrentlyLiked ->
                activity?.runOnUiThread {
                    if (isCurrentlyLiked) {
                        // Unlike the review using coroutines
                        CoroutineScope(Dispatchers.Main).launch {
                            val success = reviewManager.unlikeReview(review.id, currentUserId)
                            if (success) {
                                val newLikes = maxOf(0, review.likes - 1)
                                updateLikeButton(false, newLikes, likeButton, likeCountText)
                                showToast("Review unliked")
                            } else {
                                showToast("Failed to unlike review")
                            }
                        }
                    } else {
                        // Like the review using coroutines
                        CoroutineScope(Dispatchers.Main).launch {
                            val success = reviewManager.likeReview(review.id, currentUserId)
                            if (success) {
                                val newLikes = review.likes + 1
                                updateLikeButton(true, newLikes, likeButton, likeCountText)
                                showToast("Review liked")
                            } else {
                                showToast("Failed to like review")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateLikeButton(isLiked: Boolean, likeCount: Int, likeButton: ImageView, likeCountText: TextView) {
        if (isLiked) {
            likeButton.setImageResource(R.drawable.ic_heart_orange)
            likeButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.orange_500))
        } else {
            likeButton.setImageResource(R.drawable.ic_heart)
            likeButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.grey_400))
        }
        likeCountText.text = "$likeCount likes"
    }

    private fun updateStarRating(view: View, rating: Int) {
        val stars = listOf(
            view.findViewById<ImageView>(R.id.star1),
            view.findViewById<ImageView>(R.id.star2),
            view.findViewById<ImageView>(R.id.star3),
            view.findViewById<ImageView>(R.id.star4),
            view.findViewById<ImageView>(R.id.star5)
        )

        val activeColor = ContextCompat.getColor(requireContext(), R.color.purple_500)
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.gray_400)

        stars.forEachIndexed { index, star ->
            val color = if (index < rating) activeColor else inactiveColor
            ImageViewCompat.setImageTintList(star, android.content.res.ColorStateList.valueOf(color))
        }
    }

    private fun setupBottomActions(view: View) {
        // Reply button
        view.findViewById<View>(R.id.reply_button).setOnClickListener {
            showToast("Reply feature coming soon")
        }

        // Album button
        view.findViewById<View>(R.id.album_button).setOnClickListener {
            showToast("Navigate to album")
        }
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }
}