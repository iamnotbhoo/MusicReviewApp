package student.projects.musicreviewapp.ui.home

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.models.AlbumDetails
import student.projects.musicreviewapp.models.Music
import student.projects.musicreviewapp.auth.PlaylistManager
import student.projects.musicreviewapp.auth.ReviewManager
import student.projects.musicreviewapp.auth.AuthManager
import student.projects.musicreviewapp.models.Review
import student.projects.musicreviewapp.network.SpotifyApiService
import android.content.res.ColorStateList
import java.util.Calendar

class AlbumDetailFragment : Fragment() {

    private lateinit var album: Music
    private lateinit var spotifyApiService: SpotifyApiService
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var contentContainer: LinearLayout

    private lateinit var playlistManager: PlaylistManager
    private lateinit var reviewManager: ReviewManager
    private lateinit var authManager: AuthManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        spotifyApiService = SpotifyApiService(requireContext())
        playlistManager = PlaylistManager(requireContext())
        reviewManager = ReviewManager(requireContext())
        authManager = AuthManager(requireContext())
        return inflater.inflate(R.layout.fragment_album_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            album = bundle.getParcelable("album") ?: return@let
        }

        // Initialize views
        loadingIndicator = view.findViewById(R.id.loading_indicator)
        contentContainer = view.findViewById(R.id.content_container)

        setupBackButton(view)
        setupMenuButton(view)

        // Load detailed album information from Spotify
        loadAlbumDetails()
    }

    private fun loadAlbumDetails() {
        showLoading()

        spotifyApiService.getAlbumDetails(album.id, object : SpotifyApiService.SpotifyCallback<AlbumDetails> {
            override fun onSuccess(result: AlbumDetails) {
                hideLoading()
                setupViewsWithRealData(result)
                // Setup ratings system AFTER loading album data
                setupRatingsSystem()
                Log.d("AlbumDetail", "Loaded album details: ${result.title} by ${result.artist}")
            }

            override fun onError(error: String) {
                hideLoading()
                showToast("Couldn't load album details: $error")
                // Fallback to basic data
                setupViewsWithBasicData()
                // Setup ratings system with basic data
                setupRatingsSystem()
                Log.e("AlbumDetail", "Error loading album details: $error")
            }
        })
    }

    private fun setupRatingsSystem() {
        updateRatingsHistogram()
        checkUserReviewStatus()
    }

    private fun updateRatingsHistogram() {
        val histogramContainer = view?.findViewById<LinearLayout>(R.id.histogram_container)
        val ratingText = view?.findViewById<TextView>(R.id.album_rating_text)

        // Get all reviews for this album
        val reviews = getReviewsForAlbum(album.id)

        Log.d("RatingsDebug", "Found ${reviews.size} reviews for album ${album.id}")

        if (reviews.isEmpty()) {
            // No ratings yet - show all bars at zero
            Log.d("RatingsDebug", "No reviews found, showing empty histogram")
            populateEmptyHistogram(histogramContainer)
            ratingText?.text = "0.0"
        } else {
            // Calculate rating distribution
            val ratingDistribution = calculateRatingDistribution(reviews)
            Log.d("RatingsDebug", "Rating distribution: $ratingDistribution")
            populateHistogramWithData(histogramContainer, ratingDistribution)

            // Calculate average rating
            val averageRating = calculateAverageRating(reviews)
            ratingText?.text = String.format("%.1f", averageRating)
        }
    }

    private fun getReviewsForAlbum(albumId: String): List<Review> {
        return reviewManager.getReviews().filter { it.musicId == albumId }
    }

    private fun calculateRatingDistribution(reviews: List<Review>): Map<Int, Int> {
        val distribution = mutableMapOf(
            1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0
        )

        reviews.forEach { review ->
            val rating = review.rating.coerceIn(1, 5)
            distribution[rating] = distribution.getOrDefault(rating, 0) + 1
        }

        return distribution
    }

    private fun calculateAverageRating(reviews: List<Review>): Double {
        if (reviews.isEmpty()) return 0.0
        val total = reviews.sumOf { it.rating.toDouble() }
        return total / reviews.size
    }

    private fun populateEmptyHistogram(container: LinearLayout?) {
        container?.removeAllViews()

        for (rating in 5 downTo 1) {
            val bar = createHistogramBar(0, 1, rating)
            container?.addView(bar)
        }
    }

    private fun populateHistogramWithData(container: LinearLayout?, distribution: Map<Int, Int>) {
        container?.removeAllViews()

        val totalReviews = distribution.values.sum()
        val maxCount = distribution.values.maxOrNull() ?: 1

        for (rating in 5 downTo 1) {
            val count = distribution[rating] ?: 0
            val bar = createHistogramBar(count, maxCount, rating)
            container?.addView(bar)
        }
    }

    private fun createHistogramBar(count: Int, maxCount: Int, rating: Int): View {
        val barLayout = LayoutInflater.from(requireContext())
            .inflate(R.layout.histogram_bar_item, null)

        val bar = barLayout.findViewById<View>(R.id.bar)

        // Height
        val maxBarHeight = dpToPx(80f)
        val height = if (count > 0 && maxCount > 0) {
            (count.toFloat() / maxCount * maxBarHeight).toInt()
        } else {
            dpToPx(4f)
        }

        val params = bar.layoutParams as LinearLayout.LayoutParams
        params.height = height
        bar.layoutParams = params

        val barColor = ContextCompat.getColor(requireContext(), R.color.sign_in_button)
        bar.background.setTint(barColor)

        return barLayout
    }



    private fun checkUserReviewStatus() {
        val reviewFooter = view?.findViewById<LinearLayout>(R.id.review_footer)
        val footerAlbumCover = view?.findViewById<ImageView>(R.id.footer_album_cover)
        val footerReviewText = view?.findViewById<TextView>(R.id.footer_review_text)
        val footerReviewStars = view?.findViewById<TextView>(R.id.footer_review_stars)

        // Check if current user has reviewed this album
        val userReview = getUserReviewForAlbum(album.id)

        Log.d("RatingsDebug", "User review check: ${userReview != null}")
        Log.d("RatingsDebug", "Current user ID: ${getCurrentUserId()}")

        if (userReview != null) {
            // User has reviewed - show footer
            reviewFooter?.visibility = View.VISIBLE
            Log.d("RatingsDebug", "Showing review footer with rating: ${userReview.rating}")

            // Set album cover
            if (album.coverImage.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(album.coverImage)
                    .placeholder(R.drawable.album_placeholder)
                    .error(R.drawable.album_placeholder)
                    .into(footerAlbumCover!!)
            } else {
                footerAlbumCover?.setImageResource(R.drawable.album_placeholder)
            }

            // Set review text
            footerReviewText?.text = "You've reviewed this album"

            // Set star rating
            footerReviewStars?.text = "★".repeat(userReview.rating) + "☆".repeat(5 - userReview.rating)

        } else {
            // User hasn't reviewed - hide footer
            reviewFooter?.visibility = View.GONE
            Log.d("RatingsDebug", "Hiding review footer - no user review found")
        }
    }

    private fun getUserReviewForAlbum(albumId: String): Review? {
        val currentUserId = getCurrentUserId()
        return reviewManager.getReviews().find {
            it.musicId == albumId && it.userId == currentUserId
        }
    }

    private fun getCurrentUserId(): String {
        return authManager.getCurrentUser() ?: "default_user"
    }

    override fun onResume() {
        super.onResume()
        Log.d("RatingsDebug", "AlbumDetailFragment onResume - refreshing ratings")
        setupRatingsSystem()
    }

    private fun setupViewsWithRealData(albumDetails: AlbumDetails) {
        view?.let { v ->
            val poster = v.findViewById<ImageView>(R.id.album_poster)
            val thumbnail = v.findViewById<ImageView>(R.id.album_thumbnail)
            val artistName = v.findViewById<TextView>(R.id.artist_name)
            val yearDuration = v.findViewById<TextView>(R.id.album_year_duration)
            val albumDescription = v.findViewById<TextView>(R.id.album_description)
            val albumLabel = v.findViewById<TextView>(R.id.album_label)
            val albumGenre = v.findViewById<TextView>(R.id.album_genre)
            val genreHeading = v.findViewById<TextView>(R.id.genre_heading)
            val labelHeading = v.findViewById<TextView>(R.id.label_heading)
            val listenNowButton = v.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.listen_now_button)

            // Load high-quality album art
            if (albumDetails.largeCoverImage.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(albumDetails.largeCoverImage)
                    .placeholder(R.drawable.album_placeholder)
                    .error(R.drawable.album_placeholder)
                    .into(poster)
            } else if (albumDetails.coverImage.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(albumDetails.coverImage)
                    .placeholder(R.drawable.album_placeholder)
                    .error(R.drawable.album_placeholder)
                    .into(poster)
            }

            // Load thumbnail
            if (albumDetails.coverImage.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(albumDetails.coverImage)
                    .placeholder(R.drawable.album_placeholder)
                    .error(R.drawable.album_placeholder)
                    .centerCrop()
                    .into(thumbnail)
            } else {
                thumbnail.setImageResource(R.drawable.album_placeholder)
            }

            // Set artist name
            artistName.text = albumDetails.artist

            // Set release year, duration, and track count
            yearDuration.text = "${albumDetails.formattedReleaseDate} • ${albumDetails.formattedDuration} • ${albumDetails.totalTracks} tracks"

            // LISTEN NOW BUTTON
            if (albumDetails.spotifyUrl.isNotEmpty()) {
                listenNowButton.visibility = View.VISIBLE
                listenNowButton.setOnClickListener {
                    openSpotifyAlbum(albumDetails.spotifyUrl)
                }
            } else {
                listenNowButton.visibility = View.GONE
            }

            // LABEL SECTION
            if (albumDetails.label.isNotEmpty() && albumDetails.label != "Unknown Label") {
                albumLabel.text = albumDetails.label
                albumLabel.isVisible = true
                labelHeading.isVisible = true
            } else {
                albumLabel.isVisible = false
                labelHeading.isVisible = false
            }

            // GENRE SECTION
            if (albumDetails.genres.isNotEmpty()) {
                val genresText = albumDetails.genres.joinToString(", ")
                albumGenre.text = genresText
                albumGenre.isVisible = true
                genreHeading.isVisible = true
            } else {
                albumGenre.isVisible = false
                genreHeading.isVisible = false
            }

            // ALBUM DESCRIPTION SECTION
            val description = buildString {
                append("${albumDetails.artist}'s \"${albumDetails.title}\" ")

                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val releaseYear = albumDetails.releaseDate.split("-")[0].toIntOrNull() ?: currentYear
                val yearsAgo = currentYear - releaseYear
                if (yearsAgo == 0) {
                    append("is a recent release")
                } else if (yearsAgo == 1) {
                    append("was released last year")
                } else if (yearsAgo <= 5) {
                    append("was released ${yearsAgo} years ago")
                } else {
                    append("is a ${yearsAgo}-year-old album")
                }

                if (albumDetails.genres.isNotEmpty()) {
                    val primaryGenre = albumDetails.genres.first()
                    append(" in the ${primaryGenre.toLowerCase()} genre")

                    if (albumDetails.genres.size > 1) {
                        append(", also touching on ${albumDetails.genres.drop(1).joinToString(", ").toLowerCase()}")
                    }
                }

                append(".")

                append(" The album features ${albumDetails.totalTracks} track${if (albumDetails.totalTracks > 1) "s" else ""}")
                append(" with a total runtime of ${albumDetails.formattedDuration}.")

                when {
                    albumDetails.popularity > 80 -> {
                        append(" It has achieved widespread critical and commercial success,")
                        append(" standing as one of the most popular albums in its genre.")
                    }
                    albumDetails.popularity > 60 -> {
                        append(" It has garnered significant attention and positive reception")
                        append(" from both critics and listeners alike.")
                    }
                    albumDetails.popularity > 40 -> {
                        append(" The album has developed a dedicated following")
                        append(" and continues to attract new listeners.")
                    }
                    else -> {
                        append(" This album represents an interesting piece of work")
                        append(" that showcases the artist's creative vision.")
                    }
                }
            }

            albumDescription.text = description

            // REMOVED: Don't set rating or histogram here - let setupRatingsSystem() handle it
        }
    }

    private fun openSpotifyAlbum(spotifyUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(spotifyUrl))
            startActivity(intent)
        } catch (e: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(spotifyUrl.replace("spotify:", "https://open.spotify.com/")))
                startActivity(webIntent)
            } catch (e2: Exception) {
                showToast("Could not open Spotify")
            }
        }
    }

    private fun setupViewsWithBasicData() {
        view?.let { v ->
            val poster = v.findViewById<ImageView>(R.id.album_poster)
            val thumbnail = v.findViewById<ImageView>(R.id.album_thumbnail)
            val artistName = v.findViewById<TextView>(R.id.artist_name)
            val yearDuration = v.findViewById<TextView>(R.id.album_year_duration)
            val desc = v.findViewById<TextView>(R.id.album_description)
            val albumLabel = v.findViewById<TextView>(R.id.album_label)
            val labelHeading = v.findViewById<TextView>(R.id.label_heading)
            val albumGenre = v.findViewById<TextView>(R.id.album_genre)
            val genreHeading = v.findViewById<TextView>(R.id.genre_heading)
            val listenNowButton = v.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.listen_now_button)

            // Load basic images
            if (album.coverImage.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(album.coverImage)
                    .placeholder(R.drawable.album_placeholder)
                    .error(R.drawable.album_placeholder)
                    .into(poster)

                Glide.with(requireContext())
                    .load(album.coverImage)
                    .placeholder(R.drawable.album_placeholder)
                    .error(R.drawable.album_placeholder)
                    .centerCrop()
                    .into(thumbnail)
            } else {
                poster.setImageResource(R.drawable.album_placeholder)
                thumbnail.setImageResource(R.drawable.album_placeholder)
            }

            // Basic info
            artistName.text = album.artist
            yearDuration.text = "${album.releaseYear} • ${getAlbumDuration()}mins"
            desc.text = getAlbumDescription(album)

            // Hide sections in basic data mode
            albumLabel.isVisible = false
            labelHeading.isVisible = false
            albumGenre.isVisible = false
            genreHeading.isVisible = false
            listenNowButton.visibility = View.GONE

            // REMOVED: Don't set rating or histogram here - let setupRatingsSystem() handle it
        }
    }

    private fun setupMenuButton(view: View) {
        val menuIcon = view.findViewById<ImageView>(R.id.menu_button)
        menuIcon.setOnClickListener {
            showAlbumMenuPopup()
        }
    }

    private fun showAlbumMenuPopup() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val view = layoutInflater.inflate(R.layout.bottom_sheet_album_actions, null)
        dialog.setContentView(view)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setGravity(Gravity.CENTER)

        // CONNECT BUTTONS
        val listenBtn = view.findViewById<LinearLayout>(R.id.action_listen)
        val listenIcon = view.findViewById<ImageView>(R.id.icon_listen)
        val listenText = view.findViewById<TextView>(R.id.text_listen)

        val likeBtn = view.findViewById<LinearLayout>(R.id.action_like)
        val likeIcon = view.findViewById<ImageView>(R.id.icon_like)
        val likeText = view.findViewById<TextView>(R.id.text_like)

        val reviewBtn = view.findViewById<LinearLayout>(R.id.action_review)

        val addToPlaylistBtn = view.findViewById<LinearLayout>(R.id.action_add_to_playlist)
        val addToPlaylistIcon = view.findViewById<ImageView>(R.id.icon_add_to_playlist)
        val addToPlaylistText = view.findViewById<TextView>(R.id.text_add_to_playlist)

        val stars = listOf(
            view.findViewById<ImageView>(R.id.star1),
            view.findViewById<ImageView>(R.id.star2),
            view.findViewById<ImageView>(R.id.star3),
            view.findViewById<ImageView>(R.id.star4),
            view.findViewById<ImageView>(R.id.star5)
        )

        // LISTEN toggle
        listenBtn.setOnClickListener {
            val isActive = listenText.text == "Listened"
            if (isActive) {
                listenText.text = "Listen"
                ImageViewCompat.setImageTintList(listenIcon, ColorStateList.valueOf(Color.GRAY))
            } else {
                listenText.text = "Listened"
                ImageViewCompat.setImageTintList(listenIcon, ColorStateList.valueOf(Color.parseColor("#9B59B6")))
            }
        }

        // LIKE toggle
        likeBtn.setOnClickListener {
            val isLiked = likeIcon.imageTintList?.defaultColor == Color.parseColor("#E67E22")
            if (isLiked) {
                ImageViewCompat.setImageTintList(likeIcon, ColorStateList.valueOf(Color.GRAY))
                likeText.text = "Like"
            } else {
                ImageViewCompat.setImageTintList(likeIcon, ColorStateList.valueOf(Color.parseColor("#E67E22")))
                likeText.text = "Liked"
            }
        }

        // REVIEW button
        reviewBtn.setOnClickListener {
            dialog.dismiss()
            navigateToReviewPage()
        }

        // Add to Playlist button
        addToPlaylistBtn.setOnClickListener {
            addToPlaylist()
            dialog.dismiss()
        }

        // Stars
        fun updateStars(rating: Int) {
            stars.forEachIndexed { index, star ->
                val color = if (index < rating) Color.parseColor("#9B59B6") else Color.GRAY
                ImageViewCompat.setImageTintList(star, ColorStateList.valueOf(color))
            }
        }

        stars.forEachIndexed { index, star ->
            star.setOnClickListener {
                updateStars(index + 1)
            }
        }

        // Done button
        view.findViewById<TextView>(R.id.action_done).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addToPlaylist() {
        playlistManager.addToPlaylist(album)
        showToast("Added to playlist")
    }

    private fun navigateToReviewPage() {
        val bundle = Bundle().apply {
            putParcelable("album", album)
        }
        findNavController().navigate(R.id.action_albumDetailFragment_to_reviewFragment, bundle)
    }

    private fun setupBackButton(view: View) {
        view.findViewById<ImageView>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun showLoading() {
        loadingIndicator.isVisible = true
        contentContainer.isVisible = false
    }

    private fun hideLoading() {
        loadingIndicator.isVisible = false
        contentContainer.isVisible = true
    }

    private fun getAlbumDuration(): String {
        return when (album.title.uppercase()) {
            "DAMN" -> "54"
            else -> "45"
        }
    }

    private fun getAlbumDescription(album: Music): String {
        return when (album.title.uppercase()) {
            "DAMN" -> "DAMN. is a grab-you-by-the-throat declaration that's as blunt, complex and unflinching as the name suggests."
            else -> "A compelling musical journey that showcases artistic growth and creative expression."
        }
    }

    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        ).toInt()
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }
}