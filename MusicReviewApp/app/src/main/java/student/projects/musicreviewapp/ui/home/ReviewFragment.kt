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
import student.projects.musicreviewapp.auth.ReviewManager
import student.projects.musicreviewapp.auth.AuthManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewFragment : Fragment() {

    private lateinit var album: Music
    private lateinit var reviewManager: ReviewManager
    private lateinit var authManager: AuthManager

    private var selectedRating = 0
    private var isLiked = false
    private var isFirstListen = true
    private var allowReplies = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        reviewManager = ReviewManager(requireContext())
        authManager = AuthManager(requireContext())
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
    }

    private fun updateLikeButton(likeIcon: ImageView, likeText: TextView) {
        if (isLiked) {
            ImageViewCompat.setImageTintList(likeIcon, android.content.res.ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.orange_500)))
            likeText.text = "Liked"
        } else {
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
            firstListenText.text = if (isFirstListen) "First Listen" else "Listened"
        }

        repliesLayout.setOnClickListener {
            allowReplies = !allowReplies
            repliesText.text = if (allowReplies) "Anyone can reply" else "No replies"
        }
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
    }

    private fun addTagToChipGroup(tag: String, chipGroup: ChipGroup) {
        val chip = Chip(requireContext()).apply {
            text = tag
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                chipGroup.removeView(this)
            }
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
                android.widget.Toast.makeText(requireContext(), "Please select a rating", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveReview()
        }
    }

    private fun saveReview() {
        val reviewContent = view?.findViewById<EditText>(R.id.review_input)?.text?.toString() ?: ""
        val tags = getTags(view?.findViewById(R.id.tags_chip_group) ?: return)

        if (selectedRating == 0) {
            android.widget.Toast.makeText(requireContext(), "Please select a rating", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        // Get current user info - FIXED: Use actual auth manager
        val currentUserId = authManager.getCurrentUser() ?: "default_user"
        val currentUserName = "iamnotbhoo" // You might want to get this from auth manager too

        // Create review object
        val review = Review(
            id = reviewManager.generateReviewId(),
            userId = currentUserId, // Use actual current user ID
            userName = currentUserName,
            userPhotoUrl = null,
            content = reviewContent,
            timestamp = reviewManager.getCurrentTimestamp(),
            musicId = album.id,
            musicTitle = album.title,
            musicYear = album.releaseYear.toString(),
            musicCoverUrl = album.coverImage,
            rating = selectedRating,
            tags = tags,
            isFirstListen = isFirstListen,
            allowReplies = allowReplies,
            liked = isLiked
        )

        // Save the review
        reviewManager.saveReview(review)

        android.widget.Toast.makeText(requireContext(), "Review saved successfully!", android.widget.Toast.LENGTH_SHORT).show()

        // Navigate back to album detail
        findNavController().popBackStack()
    }
}