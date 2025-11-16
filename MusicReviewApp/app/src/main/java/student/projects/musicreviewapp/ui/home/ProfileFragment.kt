package student.projects.musicreviewapp.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.auth.AuthManager
import student.projects.musicreviewapp.auth.FavoriteAlbumsManager
import student.projects.musicreviewapp.auth.ReviewManager
import student.projects.musicreviewapp.models.Music
import student.projects.musicreviewapp.models.User

class ProfileFragment : Fragment() {
    private lateinit var authManager: AuthManager
    private lateinit var favoriteAlbumsManager: FavoriteAlbumsManager
    private val favoritesAdapter = AlbumGridAdapter()
    private val recentActivityAdapter = AlbumGridAdapter()

    // User data that can be updated
    private var currentUsername = "iamnotbhoo"
    private var currentLocation = "Gotham"
    private var currentBio = "YuCk"
    private var currentPronoun = "He / his"
    private var currentUrl = "iamnotbhoo.co.za"
    private var currentProfileImageUri: Uri? = null

    // Gallery launcher
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            currentProfileImageUri = selectedUri
            updateProfileImage(selectedUri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authManager = AuthManager(requireContext())
        favoriteAlbumsManager = FavoriteAlbumsManager(requireContext())

        setupViews(view)
        setupProfileStats(view)
        loadProfileData()
    }

    private fun setupViews(view: View) {
        // Setup RecyclerViews with horizontal layout
        val favoritesRecycler = view.findViewById<RecyclerView>(R.id.favorites_recycler)
        favoritesRecycler.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = favoritesAdapter
        }

        val recentActivityRecycler = view.findViewById<RecyclerView>(R.id.recent_activity_recycler)
        recentActivityRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = recentActivityAdapter
        }

        // Settings button
        view.findViewById<ImageView>(R.id.settings_button)?.setOnClickListener {
            showSettingsBottomSheet()
        }

        // Profile image click for changing avatar
        view.findViewById<ImageView>(R.id.profile_image)?.setOnClickListener {
            showChangeAvatarDialog()
        }

        view.findViewById<TextView>(R.id.more_activity_button)?.setOnClickListener {
            showToast("View all activity")
        }
    }

    private fun showSettingsBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_settings, null)
        dialog.setContentView(view)
        dialog.behavior.peekHeight = resources.displayMetrics.heightPixels
        dialog.show()

        // Set current values
        view.findViewById<TextView>(R.id.current_username)?.text = currentUsername
        view.findViewById<TextView>(R.id.current_location)?.text = currentLocation
        view.findViewById<TextView>(R.id.current_bio)?.text = currentBio
        view.findViewById<TextView>(R.id.current_pronoun)?.text = currentPronoun
        view.findViewById<TextView>(R.id.current_url)?.text = currentUrl

        // Close button
        view.findViewById<ImageView>(R.id.close_button)?.setOnClickListener {
            dialog.dismiss()
        }

        // Sign out option
        view.findViewById<View>(R.id.sign_out_option)?.setOnClickListener {
            dialog.dismiss()
            showSignOutConfirmation()
        }

        // Username option
        view.findViewById<View>(R.id.username_option)?.setOnClickListener {
            showEditDialog("Username", currentUsername) { newUsername ->
                currentUsername = newUsername
                updateProfileUI()
                dialog.dismiss()
            }
        }

        // Location option
        view.findViewById<View>(R.id.location_option)?.setOnClickListener {
            showEditDialog("Location", currentLocation) { newLocation ->
                currentLocation = newLocation
                updateProfileUI()
                dialog.dismiss()
            }
        }

        // Bio option
        view.findViewById<View>(R.id.bio_option)?.setOnClickListener {
            showEditDialog("Bio", currentBio) { newBio ->
                currentBio = newBio
                updateProfileUI()
                dialog.dismiss()
            }
        }

        // Pronoun option
        view.findViewById<View>(R.id.pronoun_option)?.setOnClickListener {
            showPronounDialog()
        }

        // URL option
        view.findViewById<View>(R.id.url_option)?.setOnClickListener {
            showEditDialog("URL", currentUrl) { newUrl ->
                currentUrl = newUrl
                updateProfileUI()
                dialog.dismiss()
            }
        }

        // Favorite Albums option
        view.findViewById<View>(R.id.favorite_albums_option)?.setOnClickListener {
            dialog.dismiss()
            navigateToFavoriteAlbumsManager()
        }

        // Change Avatar option
        view.findViewById<View>(R.id.change_avatar_option)?.setOnClickListener {
            dialog.dismiss()
            showChangeAvatarDialog()
        }
    }

    private fun showEditDialog(title: String, currentValue: String, onSave: (String) -> Unit) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_text, null)
        val editText = dialogView.findViewById<EditText>(R.id.edit_text)
        editText.setText(currentValue)
        editText.setSelection(currentValue.length)

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newValue = editText.text.toString().trim()
                if (newValue.isNotEmpty()) {
                    onSave(newValue)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPronounDialog() {
        val pronouns = arrayOf("He / his", "She / her", "They / them", "Prefer not to say")
        var selectedIndex = pronouns.indexOf(currentPronoun)
        if (selectedIndex == -1) selectedIndex = 0

        AlertDialog.Builder(requireContext())
            .setTitle("Pronoun")
            .setSingleChoiceItems(pronouns, selectedIndex) { dialog, which ->
                currentPronoun = pronouns[which]
                updateProfileUI()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showChangeAvatarDialog() {
        val options = arrayOf("Choose from Gallery", "Remove Current")

        AlertDialog.Builder(requireContext())
            .setTitle("Change Avatar")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openGallery()
                    1 -> {
                        // Reset to default avatar
                        currentProfileImageUri = null
                        view?.findViewById<ImageView>(R.id.profile_image)?.setImageResource(R.drawable.placeholder_profile)
                        showToast("Avatar removed")
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun updateProfileImage(uri: Uri) {
        view?.findViewById<ImageView>(R.id.profile_image)?.let { profileImage ->
            Glide.with(this)
                .load(uri)
                .circleCrop()
                .placeholder(R.drawable.placeholder_profile)
                .error(R.drawable.placeholder_profile)
                .into(profileImage)
        }
        showToast("Profile picture updated")
    }

    private fun showSignOutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Sign Out") { _, _ ->
                performSignOut()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performSignOut() {
        authManager.logout()
        showToast("Signed out successfully")
        findNavController().navigate(R.id.action_profileFragment_to_welcomeFragment)
    }

    private fun updateProfileUI() {
        view?.let { v ->
            v.findViewById<TextView>(R.id.header_username)?.text = currentUsername
            v.findViewById<TextView>(R.id.user_bio)?.text = currentBio
            v.findViewById<TextView>(R.id.user_location)?.text = currentLocation
            v.findViewById<TextView>(R.id.user_website)?.text = currentUrl
        }
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun setupProfileStats(view: View) {
        // Set up click listeners for each row
        view.findViewById<View>(R.id.albums_row)?.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_userAlbumsFragment)
        }

        view.findViewById<View>(R.id.diary_row)?.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_diaryFragment)
        }

        view.findViewById<View>(R.id.reviews_row)?.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_userReviewsFragment)
        }

        view.findViewById<View>(R.id.lists_row)?.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_userListsFragment)
        }

        view.findViewById<View>(R.id.playlists_row)?.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_playlistFragment)
        }

        view.findViewById<View>(R.id.likes_row)?.setOnClickListener {
            navigateToComingSoon("Likes")
        }

        // Set initial stats
        updateProfileStats(view)
    }

    private fun updateProfileStats(view: View) {
        // Set mock data - replace with real data from your database
        view.findViewById<TextView>(R.id.albums_count)?.text = "238 / 97 this year"
        view.findViewById<TextView>(R.id.diary_count)?.text = "110 / 110 this year"
        view.findViewById<TextView>(R.id.reviews_count)?.text = "84"
        view.findViewById<TextView>(R.id.lists_count)?.text = "2"
        view.findViewById<TextView>(R.id.playlists_count)?.text = "324"
        view.findViewById<TextView>(R.id.likes_count)?.text = "520"
    }

    private fun navigateToFavoriteAlbumsManager() {
        findNavController().navigate(R.id.action_profileFragment_to_favoriteAlbumsFragment)
    }

    private fun navigateToComingSoon(feature: String) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("$feature Feature")
            .setMessage("This feature is coming soon!")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun loadProfileData() {
        val currentUser = getCurrentUser()

        // Update UI with user data
        view?.let { v ->
            v.findViewById<TextView>(R.id.header_username)?.text = "iamnotbhoo"
            v.findViewById<TextView>(R.id.user_bio)?.text = "YuCk"
            v.findViewById<TextView>(R.id.user_badge)?.text = "PRO"

            // Set social links
            v.findViewById<TextView>(R.id.user_location)?.text = "Gotham"
            v.findViewById<TextView>(R.id.user_website)?.text = "iamnotbhoo.co.za"

            // Load profile image - use saved URI if available
            val profileImage = v.findViewById<ImageView>(R.id.profile_image)
            if (currentProfileImageUri != null) {
                Glide.with(this)
                    .load(currentProfileImageUri)
                    .circleCrop()
                    .placeholder(R.drawable.placeholder_profile)
                    .error(R.drawable.placeholder_profile)
                    .into(profileImage)
            } else {
                Glide.with(this)
                    .load(R.drawable.placeholder_profile)
                    .circleCrop()
                    .into(profileImage)
            }
        }

        // Load favorites and recent activity
        loadFavorites()
        loadRecentActivity()
    }

    private fun loadFavorites() {
        val favoriteAlbums = favoriteAlbumsManager.getFavoriteAlbums()

        // If no favorites, show empty state (or you can show placeholders)
        if (favoriteAlbums.isNotEmpty()) {
            favoritesAdapter.submitList(favoriteAlbums)
        } else {
            // Show empty state - you can show placeholder albums if you want
            favoritesAdapter.submitList(emptyList())
        }
    }

    private fun loadRecentActivity() {
        val reviewManager = ReviewManager(requireContext())
        val recentReviews = reviewManager.getRecentReviews(8)

        // Convert reviews to Music objects for display
        val recentActivityAlbums = recentReviews.map { review ->
            Music(
                id = review.musicId,
                title = review.musicTitle,
                artist = "", // You might want to store artist in review or fetch from album
                album = review.musicTitle,
                releaseYear = review.musicYear.toIntOrNull() ?: 0,
                genre = "",
                coverImage = review.musicCoverUrl ?: "",
                averageRating = review.rating.toDouble(),
                reviewCount = 1
            )
        }
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
                coverImage = localAlbumCovers[index % localAlbumCovers.size],
                genre = listOf("Pop", "Rock", "Electronic", "Indie", "Hip-Hop")[index % 5],
                releaseYear = currentYear - (index % 3),
                averageRating = (3.5 + (index % 5) * 0.3).coerceAtMost(5.0),
                reviewCount = (index + 1) * 10
            )
        }
    }

    // Use ONLY the original AlbumGridAdapter for both favorites and recent activity
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

            when {
                album.coverImage.isEmpty() -> {
                    holder.albumCover.setImageResource(R.drawable.album_placeholder)
                }
                album.coverImage.startsWith("http") -> {
                    Glide.with(holder.itemView.context)
                        .load(album.coverImage)
                        .placeholder(R.drawable.album_placeholder)
                        .error(R.drawable.album_placeholder)
                        .centerCrop()
                        .into(holder.albumCover)
                }
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