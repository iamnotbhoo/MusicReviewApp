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
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.models.Review

class ReviewDetailFragment : Fragment() {

    private lateinit var review: Review

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        // Set likes count
        view.findViewById<TextView>(R.id.likes_count).text = "${getRandomLikes()} likes"

        // Set star rating
        updateStarRating(view, review.rating)

        // Set like button state
        updateLikeButton(view, review.liked)

        // Setup bottom actions
        setupBottomActions(view)
    }

    private fun setupBackButton(view: View) {
        view.findViewById<ImageView>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupLikeButton(view: View) {
        val likeIcon = view.findViewById<ImageView>(R.id.like_icon)
        val likesCount = view.findViewById<TextView>(R.id.likes_count)

        likeIcon.setOnClickListener {
            review = review.copy(liked = !review.liked)
            updateLikeButton(view, review.liked)

            // Update likes count
            val currentLikes = likesCount.text.toString().split(" ")[0].toIntOrNull() ?: 0
            val newLikes = if (review.liked) currentLikes + 1 else currentLikes - 1
            likesCount.text = "$newLikes likes"
        }
    }

    private fun updateLikeButton(view: View, liked: Boolean) {
        val likeIcon = view.findViewById<ImageView>(R.id.like_icon)
        val color = if (liked)
            ContextCompat.getColor(requireContext(), R.color.orange_500)
        else
            ContextCompat.getColor(requireContext(), R.color.gray_400)

        ImageViewCompat.setImageTintList(likeIcon, android.content.res.ColorStateList.valueOf(color))
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
            // Show reply functionality
            android.widget.Toast.makeText(requireContext(), "Reply feature coming soon", android.widget.Toast.LENGTH_SHORT).show()
        }

        // Album button
        view.findViewById<View>(R.id.album_button).setOnClickListener {
            // Navigate to album detail
            android.widget.Toast.makeText(requireContext(), "Navigate to album", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun getRandomLikes(): Int {
        return (3..15).random()
    }
}