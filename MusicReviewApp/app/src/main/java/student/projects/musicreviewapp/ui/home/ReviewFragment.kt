package student.projects.musicreviewapp.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.models.Music
import student.projects.musicreviewapp.models.Review
import student.projects.musicreviewapp.auth.AuthManager
import student.projects.musicreviewapp.auth.FirebaseReviewManager
import java.text.SimpleDateFormat
import java.util.*

class ReviewFragment : Fragment() {

    private lateinit var album: Music
    private lateinit var reviewManager: FirebaseReviewManager
    private lateinit var authManager: AuthManager

    private var selectedRating = 0
    private var isLiked = false
    private var isFirstListen = true
    private var allowReplies = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        reviewManager = FirebaseReviewManager(requireContext())
        authManager = AuthManager()
        return inflater.inflate(R.layout.fragment_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            album = bundle.getParcelable("album") ?: return@let
        }

        setupViews(view)
        setupBackButton(view)
        setupRatingStars(view)
        setupLikeButton(view)
        setupReviewOptions(view)
        setupSaveButton(view)
    }

    private fun setupViews(view: View) {
        // Set album title
        view.findViewById<TextView>(R.id.album_title).text = album.title

        // Set current date
        val currentDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
        view.findViewById<TextView>(R.id.review_date).text = currentDate

        // Load album cover
        val albumCover = view.findViewById<ImageView>(R.id.album_cover)
        if (album.coverImage.isNotEmpty()) {
            Glide.with(requireContext())
                .load(album.coverImage)
                .placeholder(R.drawable.album_placeholder)
                .error(R.drawable.album_placeholder)
                .into(albumCover)
        } else {
            albumCover.setImageResource(R.drawable.album_placeholder)
        }

        // Setup tags input
        setupTagsInput(view)
    }

    private fun setupBackButton(view: View) {
        view.findViewById<ImageView>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRatingStars(view: View) {
        val stars = listOf(
            view.findViewById<ImageView>(R.id.star1),
            view.findViewById<ImageView>(R.id.star2),
            view.findViewById<ImageView>(R.id.star3),
            view.findViewById<ImageView>(R.id.star4),
            view.findViewById<ImageView>(R.id.star5)
        )

        fun updateStars(rating: Int) {
            stars.forEachIndexed { index, star ->
                val color = if (index < rating)
                    ContextCompat.getColor(requireContext(), R.color.purple_500)
                else
                    ContextCompat.getColor(requireContext(), R.color.grey_400)
                ImageViewCompat.setImageTintList(star, android.content.res.ColorStateList.valueOf(color))
            }
            selectedRating = rating
        }

        stars.forEachIndexed { index, star ->
            star.setOnClickListener {
                updateStars(index + 1)
            }
        }

        // Initialize with 0 stars
        updateStars(0)
    }

    private fun setupLikeButton(view: View) {
        val likeIcon = view.findViewById<ImageView>(R.id.like_icon)
        val likeText = view.findViewById<TextView>(R.id.like_text)

        likeIcon.setOnClickListener {
            isLiked = !isLiked
            updateLikeButton(likeIcon, likeText)
        }

        likeText.setOnClickListener {
            isLiked = !isLiked
            updateLikeButton(likeIcon, likeText)
        }

        // Initialize like button state
        updateLikeButton(likeIcon, likeText)
    }

    private fun updateLikeButton(likeIcon: ImageView, likeText: TextView) {
        if (isLiked) {
            likeIcon.setImageResource(R.drawable.ic_heart_orange)
            ImageViewCompat.setImageTintList(likeIcon, android.content.res.ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.orange_500)))
            likeText.text = "Liked"
        } else {
            likeIcon.setImageResource(R.drawable.ic_heart)
            ImageViewCompat.setImageTintList(likeIcon, android.content.res.ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.grey_400)))
            likeText.text = "Like"
        }
    }

    private fun setupReviewOptions(view: View) {
        val firstListenLayout = view.findViewById<LinearLayout>(R.id.first_listen_layout)
        val firstListenText = view.findViewById<TextView>(R.id.first_listen_text)

        val repliesLayout = view.findViewById<LinearLayout>(R.id.replies_layout)
        val repliesText = view.findViewById<TextView>(R.id.replies_text)

        firstListenLayout.setOnClickListener {
            isFirstListen = !isFirstListen
            updateFirstListenOption(firstListenText)
        }

        repliesLayout.setOnClickListener {
            allowReplies = !allowReplies
            updateRepliesOption(repliesText)
        }

        // Initialize options
        updateFirstListenOption(firstListenText)
        updateRepliesOption(repliesText)
    }

    private fun updateFirstListenOption(firstListenText: TextView) {
        firstListenText.text = if (isFirstListen) "First Listen" else "Listened Before"
    }

    private fun updateRepliesOption(repliesText: TextView) {
        repliesText.text = if (allowReplies) "Anyone can reply" else "No replies"
    }

    private fun setupTagsInput(view: View) {
        val tagsInput = view.findViewById<EditText>(R.id.tags_input)
        val tagsChipGroup = view.findViewById<ChipGroup>(R.id.tags_chip_group)

        tagsInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s?.toString()?.endsWith(",") == true || s?.toString()?.endsWith(" ") == true) {
                    val tag = s.toString().trim().removeSuffix(",").removeSuffix(" ")
                    if (tag.isNotEmpty()) {
                        addTagToChipGroup(tag, tagsChipGroup)
                        tagsInput.text.clear()
                    }
                }
            }
        })

        // Add hint text
        tagsInput.hint = "Add tags (press comma or space to add)"
    }

    private fun addTagToChipGroup(tag: String, chipGroup: ChipGroup) {
        val chip = Chip(requireContext()).apply {
            text = tag
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                chipGroup.removeView(this)
            }
            // Style the chip
            chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.purple_200)
            )
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
        chipGroup.addView(chip)
    }

    private fun getTags(chipGroup: ChipGroup): List<String> {
        val tags = mutableListOf<String>()
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as? Chip
            chip?.text?.toString()?.let { tags.add(it) }
        }
        return tags
    }

    private fun setupSaveButton(view: View) {
        val saveButton = view.findViewById<TextView>(R.id.save_button)

        saveButton.setOnClickListener {
            if (selectedRating == 0) {
                showToast("Please select a rating")
                return@setOnClickListener
            }

            val reviewContent = view.findViewById<EditText>(R.id.review_input)?.text?.toString() ?: ""
            if (reviewContent.isBlank()) {
                showToast("Please write a review")
                return@setOnClickListener
            }

            saveReview()
        }

        // Add text change listener to enable/disable save button
        val reviewInput = view.findViewById<EditText>(R.id.review_input)
        reviewInput?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val hasContent = s?.toString()?.isNotBlank() == true
                val hasRating = selectedRating > 0
                saveButton.isEnabled = hasContent && hasRating
                saveButton.alpha = if (hasContent && hasRating) 1.0f else 0.5f
            }
        })
    }

    private fun saveReview() {
        val reviewContent = view?.findViewById<EditText>(R.id.review_input)?.text?.toString() ?: ""
        val tags = getTags(view?.findViewById(R.id.tags_chip_group) ?: return)

        val currentUserId = authManager.getCurrentUid() ?: run {
            showToast("Not logged in")
            return
        }

        val currentUsername = authManager.getCurrentUser() ?: "iamnotbhoo"

        // Show loading state
        view?.findViewById<TextView>(R.id.save_button)?.text = "Saving..."

        // Create review object
        val review = Review(
            id = reviewManager.generateReviewId(),
            userId = currentUserId,
            userName = currentUsername,
            userPhotoUrl = null,
            content = reviewContent,
            timestamp = reviewManager.getCurrentTimestamp(),
            musicId = album.id,
            musicTitle = album.title,
            musicArtist = album.artist,
            musicYear = album.releaseYear.toString(),
            musicCoverUrl = album.coverImage,
            rating = selectedRating,
            tags = tags,
            isFirstListen = isFirstListen,
            allowReplies = allowReplies,
            liked = isLiked,
            likes = 0
        )

        // Save the review to Firebase
        reviewManager.saveReview(review) { success ->
            activity?.runOnUiThread {
                if (success) {
                    showToast("Review saved successfully!")

                    // Navigate back after a short delay
                    view?.postDelayed({
                        findNavController().popBackStack()
                    }, 1000)

                } else {
                    showToast("Failed to save review. Please try again.")
                    // Reset save button
                    view?.findViewById<TextView>(R.id.save_button)?.text = "Save Review"
                }
            }
        }
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up any resources if needed
    }
}