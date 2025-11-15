package student.projects.musicreviewapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.auth.AuthManager
import student.projects.musicreviewapp.models.Music
import student.projects.musicreviewapp.models.User

class ProfileFragment : Fragment() {
    private lateinit var authManager: AuthManager
    private val favoritesAdapter = AlbumGridAdapter()
    private val recentActivityAdapter = AlbumGridAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authManager = AuthManager(requireContext())

        setupViews(view)
        loadProfileData()
    }

    private fun setupViews(view: View) {
        // Setup RecyclerViews with horizontal layout
        val favoritesRecycler = view.findViewById<RecyclerView>(R.id.favorites_recycler)
        favoritesRecycler.apply {
            layoutManager =
                GridLayoutManager(requireContext(), 1, GridLayoutManager.HORIZONTAL, false)
            adapter = favoritesAdapter
        }

        val recentActivityRecycler = view.findViewById<RecyclerView>(R.id.recent_activity_recycler)
        recentActivityRecycler.apply {
            layoutManager =
                GridLayoutManager(requireContext(), 1, GridLayoutManager.HORIZONTAL, false)
            adapter = recentActivityAdapter
        }

        // FIXED: Navigate to home fragment with proper back stack handling
        view.findViewById<ImageView>(R.id.back_button)?.setOnClickListener {
            // Navigate to home and clear the back stack for profile
            findNavController().navigate(R.id.homeFragment)
        }

        view.findViewById<TextView>(R.id.more_activity_button)?.setOnClickListener {
            android.widget.Toast.makeText(
                requireContext(),
                "View all activity",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        // Menu button
        view.findViewById<ImageView>(R.id.menu_button)?.setOnClickListener {
            android.widget.Toast.makeText(
                requireContext(),
                "Menu options",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadProfileData() {
        val currentUser = getCurrentUser()

        // Update UI with user data
        view?.let { v ->
            v.findViewById<TextView>(R.id.header_username)?.text = "iamnotbhoo"
            v.findViewById<TextView>(R.id.user_bio)?.text = "YuCk"
            v.findViewById<TextView>(R.id.user_badge)?.text = "PRO"
            v.findViewById<TextView>(R.id.favorites_count)?.text = "4"
            v.findViewById<TextView>(R.id.recent_activity_count)?.text = "9"

            // Set social links
            v.findViewById<TextView>(R.id.user_location)?.text = "Gotham"
            v.findViewById<TextView>(R.id.user_website)?.text = "iamnotbhoo.co.za"
            v.findViewById<TextView>(R.id.user_twitter)?.text = "@iamnotbhoo"

            // Load profile image
            val profileImage = v.findViewById<ImageView>(R.id.profile_image)
            Glide.with(this)
                .load(R.drawable.placeholder_profile)
                .circleCrop()
                .into(profileImage)
        }

        // Load favorites and recent activity
        loadFavorites()
        loadRecentActivity()
    }

    private fun loadFavorites() {
        val mockFavorites = getMockAlbums(4) // Changed from 9 to 4
        favoritesAdapter.submitList(mockFavorites)
        view?.findViewById<TextView>(R.id.favorites_count)?.text = "4" // Update count
    }

    private fun loadRecentActivity() {
        val mockRecent = getMockAlbums(8).shuffled()
        recentActivityAdapter.submitList(mockRecent)
        view?.findViewById<TextView>(R.id.recent_activity_count)?.text = "8"
    }

    private fun getCurrentUser(): User {
        val userName = authManager.getCurrentUser() ?: "iamnotbhoo"
        return User(
            id = "1",
            username = userName,
            email = "user@example.com",
            profilePicture = null,
            bio = "YuCk"
        )
    }

    private fun getMockAlbums(count: Int): List<Music> {
        // Use just the drawable resource names (without R.drawable. prefix)
        val localAlbumCovers = listOf(
            "rodeo_travis",
            "damn_kendrick",
            "dayattheraces_queen",
            "guarddog_searows",
            "blueprint_jay",
            "utopia_travis"
        )

        val albumTitles = listOf(
            "Rodeo", "Damn", "A Day At The Races",
            "Guard Dog", "The Blueprint", "Utopia"
        )

        val artists = listOf(
            "Travis Scott", "Kendrick Lamar", "Queen",
            "Searows", "Jay Z", "Travis Scott"
        )

        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        return List(count) { index ->
            Music(
                id = "album_$index",
                title = albumTitles[index % albumTitles.size],
                artist = artists[index % artists.size],
                album = albumTitles[index % albumTitles.size],
                coverImage = localAlbumCovers[index % localAlbumCovers.size], // Just the name, no "local://"
                genre = listOf("Pop", "Rock", "Electronic", "Indie", "Hip-Hop")[index % 5],
                releaseYear = currentYear - (index % 3),
                averageRating = (3.5 + (index % 5) * 0.3).coerceAtMost(5.0),
                reviewCount = (index + 1) * 10
            )
        }
    }

    class AlbumGridAdapter : RecyclerView.Adapter<AlbumGridAdapter.AlbumViewHolder>() {
        private var albums = listOf<Music>()
        var onAlbumClick: ((String) -> Unit)? = null

        class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val albumCover: ImageView = itemView.findViewById(R.id.album_cover)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_album_grid, parent, false)
            return AlbumViewHolder(view)
        }

        override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
            val album = albums[position]

            // Smart loading that handles both local resources and URLs
            when {
                // If coverImage is empty or invalid, use placeholder
                album.coverImage.isEmpty() -> {
                    holder.albumCover.setImageResource(R.drawable.album_placeholder)
                }
                // If it starts with http, it's a URL
                album.coverImage.startsWith("http") -> {
                    Glide.with(holder.itemView.context)
                        .load(album.coverImage)
                        .placeholder(R.drawable.album_placeholder)
                        .error(R.drawable.album_placeholder)
                        .centerCrop()
                        .into(holder.albumCover)
                }
                // Otherwise, assume it's a local resource name and try to load it
                else -> {
                    try {
                        val resourceId = holder.itemView.context.resources.getIdentifier(
                            album.coverImage,
                            "drawable",
                            holder.itemView.context.packageName
                        )
                        if (resourceId != 0) {
                            Glide.with(holder.itemView.context)
                                .load(resourceId)
                                .placeholder(R.drawable.album_placeholder)
                                .error(R.drawable.album_placeholder)
                                .centerCrop()
                                .into(holder.albumCover)
                        } else {
                            holder.albumCover.setImageResource(R.drawable.album_placeholder)
                        }
                    } catch (e: Exception) {
                        holder.albumCover.setImageResource(R.drawable.album_placeholder)
                    }
                }
            }

            holder.itemView.setOnClickListener {
                onAlbumClick?.invoke(album.id)
            }
        }

        override fun getItemCount(): Int = albums.size

        fun submitList(newAlbums: List<Music>) {
            albums = newAlbums
            notifyDataSetChanged()
        }
    }
}