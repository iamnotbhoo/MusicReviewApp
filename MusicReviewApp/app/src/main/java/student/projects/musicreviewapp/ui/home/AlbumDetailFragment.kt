package student.projects.musicreviewapp.ui.home

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import student.projects.musicreviewapp.network.SpotifyApiService
import android.content.res.ColorStateList
import java.util.Calendar

class AlbumDetailFragment : Fragment() {

    private lateinit var album: Music
    private lateinit var spotifyApiService: SpotifyApiService
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var contentContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        spotifyApiService = SpotifyApiService(requireContext())
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
                Log.d("AlbumDetail", "Loaded album details: ${result.title} by ${result.artist}")
            }

            override fun onError(error: String) {
                hideLoading()
                showToast("Couldn't load album details: $error")
                // Fallback to basic data
                setupViewsWithBasicData()
                Log.e("AlbumDetail", "Error loading album details: $error")
            }
        })
    }

    private fun setupViewsWithRealData(albumDetails: AlbumDetails) {
        view?.let { v ->
            val poster = v.findViewById<ImageView>(R.id.album_poster)
            val thumbnail = v.findViewById<ImageView>(R.id.album_thumbnail)
            val artistName = v.findViewById<TextView>(R.id.artist_name)
            val yearDuration = v.findViewById<TextView>(R.id.album_year_duration)
            val albumDescription = v.findViewById<TextView>(R.id.album_description)
            val albumLabel = v.findViewById<TextView>(R.id.album_label)
            val ratingText = v.findViewById<TextView>(R.id.album_rating_text)
            val histogramContainer = v.findViewById<LinearLayout>(R.id.histogram_container)
            val labelHeading = v.findViewById<TextView>(R.id.label_heading)

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
                    .into(thumbnail)
            }

            // Set artist name
            artistName.text = albumDetails.artist

            // Set release year, duration, and track count
            yearDuration.text = "${albumDetails.formattedReleaseDate} • ${albumDetails.formattedDuration} • ${albumDetails.totalTracks} tracks"

            // ========== LABEL SECTION ==========
            if (albumDetails.label.isNotEmpty() && albumDetails.label != "Unknown Label") {
                albumLabel.text = albumDetails.label
                albumLabel.isVisible = true
                labelHeading.isVisible = true
            } else {
                albumLabel.isVisible = false
                labelHeading.isVisible = false
            }

            // ========== ALBUM DESCRIPTION SECTION ==========
            // Create a more detailed album description
            val description = buildString {
                // Start with artist and album context
                append("${albumDetails.artist}'s \"${albumDetails.title}\" ")

                // Add release year context
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

                // Add genre context
                if (albumDetails.genres.isNotEmpty()) {
                    val primaryGenre = albumDetails.genres.first()
                    append(" in the ${primaryGenre.toLowerCase()} genre")

                    if (albumDetails.genres.size > 1) {
                        append(", also touching on ${albumDetails.genres.drop(1).joinToString(", ").toLowerCase()}")
                    }
                }

                append(".")

                // Add track count context
                append(" The album features ${albumDetails.totalTracks} track${if (albumDetails.totalTracks > 1) "s" else ""}")
                append(" with a total runtime of ${albumDetails.formattedDuration}.")

                // Add popularity context
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

            // Use popularity as rating (converted to 0-5 scale)
            val popularityRating = (albumDetails.popularity / 20.0).coerceAtMost(5.0)
            ratingText.text = String.format("%.1f", popularityRating)

            // Update histogram with popularity-based data
            populateHistogramWithPopularity(histogramContainer, albumDetails.popularity)
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
            val ratingText = v.findViewById<TextView>(R.id.album_rating_text)
            val histogramContainer = v.findViewById<LinearLayout>(R.id.histogram_container)

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
                    .into(thumbnail)
            } else {
                poster.setImageResource(R.drawable.album_placeholder)
                thumbnail.setImageResource(R.drawable.album_placeholder)
            }

            // Basic info
            artistName.text = album.artist
            yearDuration.text = "${album.releaseYear} • ${getAlbumDuration()}mins"
            desc.text = getAlbumDescription(album)
            ratingText.text = String.format("%.1f", album.averageRating)

            // Hide label section in basic data mode
            albumLabel.isVisible = false
            labelHeading.isVisible = false

            // Basic histogram
            populateHistogramWithRating(histogramContainer, album.averageRating)
        }
    }

    private fun populateHistogramWithPopularity(container: LinearLayout, popularity: Int) {
        container.removeAllViews()

        // Create a distribution based on popularity (0-100 converted to histogram bars)
        val normalizedPopularity = (popularity / 10.0) // Convert to 0-10 scale
        val barHeights = IntArray(10) { i ->
            val distance = kotlin.math.abs(i - normalizedPopularity)
            val value = (10.0 - distance * 1.5).coerceAtLeast(0.0)
            (value * 8).toInt() + 10 // Scale to reasonable bar heights
        }

        val maxBarHeight = barHeights.maxOrNull() ?: 1

        for (i in barHeights.indices) {
            val bar = View(requireContext())

            val heightPx = dpToPx(barHeights[i].toFloat())
            val minHeightPx = dpToPx(10f)

            val params = LinearLayout.LayoutParams(0, heightPx.coerceAtLeast(minHeightPx))
            params.weight = 1f
            params.marginStart = dpToPx(2f)
            params.marginEnd = dpToPx(2f)
            params.gravity = android.view.Gravity.BOTTOM
            bar.layoutParams = params

            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.histogram_bar)

            // Color bars based on position (active for bars around the popularity level)
            val isActiveBar = i < normalizedPopularity
            if (isActiveBar) {
                drawable?.setTint(ContextCompat.getColor(requireContext(), android.R.color.white))
            } else {
                drawable?.setTint(Color.parseColor("#404040"))
            }

            bar.background = drawable
            container.addView(bar)
        }
    }

    private fun populateHistogramWithRating(container: LinearLayout, rating: Double) {
        container.removeAllViews()

        val center = ((rating.coerceIn(0.0, 5.0) / 5.0) * 9.0).toInt()
        val baseCounts = IntArray(10) { i ->
            val dist = kotlin.math.abs(i - center)
            val value = (10.0 - dist * 2.2).coerceAtLeast(0.0)
            value.toInt()
        }

        val maxBarHeight = 90f
        val maxCount = baseCounts.maxOrNull() ?: 1
        val activeIndex = center

        for (i in baseCounts.indices) {
            val bar = View(requireContext())

            val heightPx = dpToPx(
                ((baseCounts[i].toFloat() / maxCount.toFloat()) * maxBarHeight)
            )

            val params = LinearLayout.LayoutParams(0, heightPx)
            params.weight = 1f
            params.marginStart = dpToPx(2f)
            params.marginEnd = dpToPx(2f)
            params.gravity = android.view.Gravity.BOTTOM
            bar.layoutParams = params

            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.histogram_bar)

            if (i == activeIndex) {
                drawable?.setTint(ContextCompat.getColor(requireContext(), android.R.color.white))
            } else {
                drawable?.setTint(Color.parseColor("#404040"))
            }

            bar.background = drawable
            container.addView(bar)
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

        // ===== CONNECT BUTTONS =====
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

        // REVIEW button - navigate to review page
        reviewBtn.setOnClickListener {
            dialog.dismiss()
            navigateToReviewPage()
        }

        // Add to Playlist button
        addToPlaylistBtn.setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Added to playlist", android.widget.Toast.LENGTH_SHORT).show()
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