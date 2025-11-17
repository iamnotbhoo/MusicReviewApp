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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.models.Music
import student.projects.musicreviewapp.models.Review
import student.projects.musicreviewapp.auth.ReviewManager

class UserAlbumsFragment : Fragment() {

    private lateinit var albumsAdapter: UserAlbumsAdapter
    private lateinit var reviewManager: ReviewManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_albums, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reviewManager = ReviewManager(requireContext())

        setupViews(view)
        loadUserAlbums()
    }

    private fun setupViews(view: View) {
        // Setup back button
        view.findViewById<ImageView>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }

        // Setup RecyclerView
        val albumsRecycler = view.findViewById<RecyclerView>(R.id.albums_recycler)
        albumsAdapter = UserAlbumsAdapter()

        // Set click listener for albums
        albumsAdapter.onAlbumClick = { userAlbum ->
            navigateToReviewDetail(userAlbum)
        }

        albumsRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = albumsAdapter
        }
    }

    private fun navigateToReviewDetail(userAlbum: UserAlbum) {
        val review = Review(
            id = userAlbum.reviewId,
            userId = "1",
            userName = "iamnotbhoo",
            userPhotoUrl = null,
            content = userAlbum.reviewContent,
            timestamp = userAlbum.timestamp,
            musicId = userAlbum.music.id,
            musicTitle = userAlbum.music.title,
            musicYear = userAlbum.music.releaseYear.toString(),
            musicCoverUrl = userAlbum.music.coverImage,
            rating = userAlbum.userRating,
            tags = emptyList(),
            liked = userAlbum.isLiked
        )

        val bundle = Bundle().apply {
            putParcelable("review", review)
        }
        findNavController().navigate(R.id.action_userAlbumsFragment_to_reviewDetailFragment, bundle)
    }

    private fun loadUserAlbums() {
        // Get actual reviews from ReviewManager
        val userReviews = reviewManager.getReviews()

        // Convert reviews to UserAlbum objects
        val userAlbums = userReviews.map { review ->
            UserAlbum(
                music = Music(
                    id = review.musicId,
                    title = review.musicTitle,
                    artist = "", // You might want to store artist in review
                    album = review.musicTitle,
                    releaseYear = review.musicYear.toIntOrNull() ?: 0,
                    genre = "",
                    coverImage = review.musicCoverUrl ?: "",
                    averageRating = review.rating.toDouble(),
                    reviewCount = 1
                ),
                userRating = review.rating,
                hasReview = review.content.isNotEmpty(),
                isLiked = review.liked,
                reviewId = review.id,
                reviewContent = review.content,
                timestamp = review.timestamp
            )
        }

        albumsAdapter.submitList(userAlbums)
    }

    // Update UserAlbum data class to include review info
    data class UserAlbum(
        val music: Music,
        val userRating: Int,
        val hasReview: Boolean,
        val isLiked: Boolean,
        val reviewId: String,
        val reviewContent: String,
        val timestamp: String
    )

    class UserAlbumsAdapter : RecyclerView.Adapter<UserAlbumsAdapter.AlbumViewHolder>() {
        private var albums = listOf<UserAlbum>()

        // ADDED: Click listener property
        var onAlbumClick: ((UserAlbum) -> Unit)? = null

        class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val albumCover: ImageView = itemView.findViewById(R.id.album_cover)
            val albumTitle: TextView = itemView.findViewById(R.id.album_title)
            val artistName: TextView = itemView.findViewById(R.id.artist_name)
            val star1: ImageView = itemView.findViewById(R.id.star1)
            val star2: ImageView = itemView.findViewById(R.id.star2)
            val star3: ImageView = itemView.findViewById(R.id.star3)
            val star4: ImageView = itemView.findViewById(R.id.star4)
            val star5: ImageView = itemView.findViewById(R.id.star5)
            val reviewIcon: ImageView = itemView.findViewById(R.id.review_icon)
            val likeIcon: ImageView = itemView.findViewById(R.id.like_icon)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_user_album, parent, false)
            return AlbumViewHolder(view)
        }

        override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
            val userAlbum = albums[position]
            val music = userAlbum.music

            // Load album cover
            if (!music.coverImage.isNullOrEmpty()) {
                try {
                    val resourceId = holder.itemView.context.resources.getIdentifier(
                        music.coverImage,
                        "drawable",
                        holder.itemView.context.packageName
                    )
                    if (resourceId != 0) {
                        Glide.with(holder.itemView.context)
                            .load(resourceId)
                            .placeholder(R.drawable.album_placeholder)
                            .error(R.drawable.album_placeholder)
                            .into(holder.albumCover)
                    } else {
                        holder.albumCover.setImageResource(R.drawable.album_placeholder)
                    }
                } catch (e: Exception) {
                    holder.albumCover.setImageResource(R.drawable.album_placeholder)
                }
            } else {
                holder.albumCover.setImageResource(R.drawable.album_placeholder)
            }

            // Set text content
            holder.albumTitle.text = music.title
            holder.artistName.text = music.artist

            // Update star ratings
            updateStarRating(holder, userAlbum.userRating)

            // Update review icon (show if user has written a review)
            holder.reviewIcon.visibility = if (userAlbum.hasReview) View.VISIBLE else View.GONE

            // Update like icon
            updateLikeIcon(holder, userAlbum.isLiked)

            // Set click listeners
            holder.likeIcon.setOnClickListener {
                // Toggle like state
                val newLiked = !userAlbum.isLiked
                updateLikeIcon(holder, newLiked)
                // In a real app, you'd update this in your database
            }

            // CHANGED: Album click to navigate to review detail
            holder.itemView.setOnClickListener {
                onAlbumClick?.invoke(userAlbum)
            }
        }

        private fun updateStarRating(holder: AlbumViewHolder, rating: Int) {
            val stars = listOf(holder.star1, holder.star2, holder.star3, holder.star4, holder.star5)
            val activeColor = ContextCompat.getColor(holder.itemView.context, R.color.purple_500)
            val inactiveColor = ContextCompat.getColor(holder.itemView.context, R.color.gray_400)

            stars.forEachIndexed { index, star ->
                val color = if (index < rating) activeColor else inactiveColor
                ImageViewCompat.setImageTintList(star, android.content.res.ColorStateList.valueOf(color))
            }
        }

        private fun updateLikeIcon(holder: AlbumViewHolder, liked: Boolean) {
            val color = if (liked)
                ContextCompat.getColor(holder.itemView.context, R.color.orange_500)
            else
                ContextCompat.getColor(holder.itemView.context, R.color.gray_400)

            ImageViewCompat.setImageTintList(holder.likeIcon, android.content.res.ColorStateList.valueOf(color))
        }

        override fun getItemCount(): Int = albums.size

        fun submitList(newAlbums: List<UserAlbum>) {
            albums = newAlbums
            notifyDataSetChanged()
        }
    }
}