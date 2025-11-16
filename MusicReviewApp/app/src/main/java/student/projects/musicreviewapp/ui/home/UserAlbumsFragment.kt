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

class UserAlbumsFragment : Fragment() {

    private lateinit var albumsAdapter: UserAlbumsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_albums, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        // ADDED: Set click listener for albums
        albumsAdapter.onAlbumClick = { userAlbum ->
            navigateToReviewDetail(userAlbum)
        }

        albumsRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = albumsAdapter
        }
    }

    // ADDED: Navigate to review detail
    private fun navigateToReviewDetail(userAlbum: UserAlbum) {
        // Create a review object from the album data
        val review = Review(
            id = userAlbum.music.id,
            userId = "1", // Current user ID
            userName = "iamnotbhoo",
            userPhotoUrl = null,
            content = getReviewContentForAlbum(userAlbum),
            timestamp = "Listened recently", // You can use actual timestamp
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

    // ADDED: Generate review content based on album
    private fun getReviewContentForAlbum(userAlbum: UserAlbum): String {
        return when (userAlbum.music.title) {
            "BLACK PHONE 2" -> "this is just fucking ridiculous, every album I've heard has been an absolute masterpiece. he just refuses to make a bad album."
            "WEAPONS" -> "Travis got a lot of anxiety in this one"
            "HAPPY CLINIQUE 2" -> "he's flying"
            "UTOPIA" -> "Amazing production and vibes throughout the entire album"
            "DAMN" -> "Kendrick delivers another classic with deep lyrical content"
            else -> "Great album with solid production and memorable tracks."
        }
    }

    private fun loadUserAlbums() {
        val mockAlbums = getMockAlbums()
        albumsAdapter.submitList(mockAlbums)
    }

    private fun getMockAlbums(): List<UserAlbum> {
        return listOf(
            UserAlbum(
                music = Music(
                    id = "1",
                    title = "BLACK PHONE 2",
                    artist = "HIM",
                    album = "BLACK PHONE 2",
                    releaseYear = 2024,
                    genre = "Rock",
                    coverImage = "black_phone_2",
                    averageRating = 3.5,
                    reviewCount = 1
                ),
                userRating = 3,
                hasReview = true,
                isLiked = false
            ),
            UserAlbum(
                music = Music(
                    id = "2",
                    title = "WEAPONS",
                    artist = "WEMPONS",
                    album = "WEAPONS",
                    releaseYear = 2024,
                    genre = "Electronic",
                    coverImage = "weapons_album",
                    averageRating = 3.5,
                    reviewCount = 1
                ),
                userRating = 3,
                hasReview = true,
                isLiked = true
            ),
            UserAlbum(
                music = Music(
                    id = "3",
                    title = "HAPPY CLINIQUE 2",
                    artist = "Various Artists",
                    album = "HAPPY CLINIQUE 2",
                    releaseYear = 2024,
                    genre = "Pop",
                    coverImage = "happy_clinique_2",
                    averageRating = 4.0,
                    reviewCount = 1
                ),
                userRating = 4,
                hasReview = false,
                isLiked = false
            ),
            UserAlbum(
                music = Music(
                    id = "4",
                    title = "UTOPIA",
                    artist = "Travis Scott",
                    album = "UTOPIA",
                    releaseYear = 2023,
                    genre = "Hip-Hop",
                    coverImage = "utopia_travis",
                    averageRating = 5.0,
                    reviewCount = 1
                ),
                userRating = 5,
                hasReview = true,
                isLiked = true
            ),
            UserAlbum(
                music = Music(
                    id = "5",
                    title = "DAMN",
                    artist = "Kendrick Lamar",
                    album = "DAMN",
                    releaseYear = 2017,
                    genre = "Hip-Hop",
                    coverImage = "damn_kendrick",
                    averageRating = 4.0,
                    reviewCount = 1
                ),
                userRating = 4,
                hasReview = false,
                isLiked = false
            )
        )
    }

    data class UserAlbum(
        val music: Music,
        val userRating: Int,
        val hasReview: Boolean,
        val isLiked: Boolean
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