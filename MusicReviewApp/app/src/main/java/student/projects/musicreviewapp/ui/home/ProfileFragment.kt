package student.projects.musicreviewapp.ui.home

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.auth.AuthManager
import student.projects.musicreviewapp.auth.FirebaseFavoriteAlbumsManager
import student.projects.musicreviewapp.auth.FirebaseLikeManager
import student.projects.musicreviewapp.auth.FirebaseListManager
import student.projects.musicreviewapp.auth.FirebaseReviewManager
import student.projects.musicreviewapp.auth.PlaylistManager
import student.projects.musicreviewapp.models.Music
import student.projects.musicreviewapp.models.Review
import student.projects.musicreviewapp.models.User
import student.projects.musicreviewapp.network.LanguageManager

class ProfileFragment : Fragment() {
    private lateinit var authManager: AuthManager
    private lateinit var firebaseFavoriteAlbumsManager: FirebaseFavoriteAlbumsManager
    private lateinit var firebaseReviewManager: FirebaseReviewManager
    private lateinit var firebaseListManager: FirebaseListManager
    private lateinit var firebaseLikeManager: FirebaseLikeManager
    private lateinit var playlistManager: PlaylistManager
    private lateinit var languageManager: LanguageManager

    private val favoritesAdapter = AlbumGridAdapter()
    private val recentActivityAdapter = RecentActivityAdapter()

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

        // Initialize all managers first
        authManager = AuthManager()
        firebaseFavoriteAlbumsManager = FirebaseFavoriteAlbumsManager(requireContext())
        firebaseReviewManager = FirebaseReviewManager(requireContext())
        firebaseListManager = FirebaseListManager(requireContext())
        firebaseLikeManager = FirebaseLikeManager(requireContext())
        playlistManager = PlaylistManager(requireContext())
        languageManager = LanguageManager(requireContext())

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

        // Set click listeners for adapters
        favoritesAdapter.onAlbumClick = { music ->
            navigateToAlbumDetail(music)
        }

        recentActivityAdapter.onReviewClick = { review ->
            navigateToReviewDetail(review)
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
            // Navigate to user reviews page to see all activity
            findNavController().navigate(R.id.action_profileFragment_to_userReviewsFragment)
        }
    }

    private fun navigateToAlbumDetail(music: Music) {
        val bundle = Bundle().apply {
            putParcelable("album", music)
        }
        findNavController().navigate(R.id.action_profileFragment_to_albumDetailFragment, bundle)
    }

    private fun navigateToReviewDetail(review: Review) {
        val bundle = Bundle().apply {
            putParcelable("review", review)
        }
        findNavController().navigate(R.id.action_userReviewsFragment_to_reviewDetailFragment, bundle)
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
        view.findViewById<TextView>(R.id.current_language)?.text = languageManager.getCurrentLanguageDisplayName()

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

        // Language option
        view.findViewById<View>(R.id.language_option)?.setOnClickListener {
            showLanguageDialog()
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

    private fun showLanguageDialog() {
        val languages = languageManager.getAvailableLanguages()
        val languageNames = languages.map { it.nativeName }.toTypedArray()
        val currentLanguage = languageManager.getCurrentLanguage()

        val currentIndex = languages.indexOfFirst { it.code == currentLanguage }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.select_language))
            .setSingleChoiceItems(languageNames, currentIndex) { dialog, which ->
                val selectedLanguage = languages[which]
                languageManager.setLanguage(selectedLanguage.code)

                // Immediately recreate the activity to apply language changes
                activity?.recreate()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
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
                        requireView().findViewById<ImageView>(R.id.profile_image)?.setImageResource(R.drawable.placeholder_profile)
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
        requireView().findViewById<ImageView>(R.id.profile_image)?.let { profileImage ->
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
        requireView().let { v ->
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
            findNavController().navigate(R.id.action_profileFragment_to_likesFragment)
        }

        // Load stats with real data
        updateProfileStats(view)
    }

    private fun updateProfileStats(view: View) {
        firebaseFavoriteAlbumsManager.getFavoriteAlbums { favoriteAlbums ->
            firebaseReviewManager.getReviewCount { reviewCount ->
                val playlist = playlistManager.getPlaylist()
                firebaseListManager.getLists { lists ->
                    firebaseLikeManager.getLikedAlbums { likedAlbumIds ->
                        firebaseLikeManager.getLikedReviews { likedReviewIds ->
                            firebaseLikeManager.getLikedLists { likedListIds ->
                                val totalLikesCount = likedAlbumIds.size + likedReviewIds.size + likedListIds.size

                                activity?.runOnUiThread {
                                    view.findViewById<TextView>(R.id.albums_count)?.text = "${favoriteAlbums.size} / $reviewCount this year"
                                    view.findViewById<TextView>(R.id.diary_count)?.text = "$reviewCount / $reviewCount this year"
                                    view.findViewById<TextView>(R.id.reviews_count)?.text = reviewCount.toString()
                                    view.findViewById<TextView>(R.id.lists_count)?.text = lists.size.toString()
                                    view.findViewById<TextView>(R.id.playlists_count)?.text = playlist.size.toString()
                                    view.findViewById<TextView>(R.id.likes_count)?.text = totalLikesCount.toString()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun navigateToFavoriteAlbumsManager() {
        findNavController().navigate(R.id.action_profileFragment_to_favoriteAlbumsFragment)
    }

    private fun loadProfileData() {
        val currentUser = getCurrentUser()

        // Update UI with user data
        requireView().let { v ->
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

        // Load favorites
        firebaseFavoriteAlbumsManager.getFavoriteAlbums { favoriteAlbums ->
            activity?.runOnUiThread {
                favoritesAdapter.submitList(favoriteAlbums)
            }
        }

        // Load recent activity
        firebaseReviewManager.getRecentReviews(8) { recentReviews ->
            activity?.runOnUiThread {
                recentActivityAdapter.submitList(recentReviews)
            }
        }

        // Update stats once with all data
        updateProfileStats(requireView())
    }

    private fun loadFavorites() {
        firebaseFavoriteAlbumsManager.getFavoriteAlbums { favoriteAlbums ->
            activity?.runOnUiThread {
                // If no favorites, show empty state (or you can show placeholders)
                if (favoriteAlbums.isNotEmpty()) {
                    favoritesAdapter.submitList(favoriteAlbums)
                } else {
                    // Show empty state - you can show placeholder albums if you want
                    favoritesAdapter.submitList(emptyList())
                }
            }
        }
    }

    private fun loadRecentActivity() {
        firebaseReviewManager.getRecentReviews(8) { recentReviews ->
            activity?.runOnUiThread {
                recentActivityAdapter.submitList(recentReviews)
            }
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

    // AlbumGridAdapter
    class AlbumGridAdapter : RecyclerView.Adapter<AlbumGridAdapter.AlbumViewHolder>() {
        private var albums = listOf<Music>()
        var onAlbumClick: ((Music) -> Unit)? = null

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
                onAlbumClick?.invoke(album)
            }
        }

        override fun getItemCount(): Int = albums.size

        fun submitList(newAlbums: List<Music>) {
            albums = newAlbums
            notifyDataSetChanged()
        }
    }

    // RecentActivityAdapter
    class RecentActivityAdapter : RecyclerView.Adapter<RecentActivityAdapter.RecentActivityViewHolder>() {
        private var reviews = listOf<Review>()
        var onReviewClick: ((Review) -> Unit)? = null

        class RecentActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val albumCover: ImageView = itemView.findViewById(R.id.album_cover)
            val starViews = listOf(
                itemView.findViewById<ImageView>(R.id.star1),
                itemView.findViewById<ImageView>(R.id.star2),
                itemView.findViewById<ImageView>(R.id.star3),
                itemView.findViewById<ImageView>(R.id.star4),
                itemView.findViewById<ImageView>(R.id.star5)
            )
            val reviewIcon: ImageView = itemView.findViewById(R.id.review_icon)
            val listenedIcon: ImageView = itemView.findViewById(R.id.listened_icon)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentActivityViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_recent_activity, parent, false)
            return RecentActivityViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecentActivityViewHolder, position: Int) {
            val review = reviews[position]
            val context = holder.itemView.context

            // Load album cover
            Glide.with(context)
                .load(review.musicCoverUrl)
                .placeholder(R.drawable.album_placeholder)
                .error(R.drawable.album_placeholder)
                .centerCrop()
                .into(holder.albumCover)

            // Set stars
            val activeColor = ContextCompat.getColor(context, R.color.sign_in_button)
            val inactiveColor = ContextCompat.getColor(context, R.color.gray_400)

            holder.starViews.forEachIndexed { index, star ->
                val color = if (index < review.rating) activeColor else inactiveColor
                ImageViewCompat.setImageTintList(
                    star,
                    android.content.res.ColorStateList.valueOf(color)
                )
            }

            // Show review icon if there's written content
            holder.reviewIcon.visibility =
                if (review.content.isNotEmpty()) View.VISIBLE else View.GONE

            // Show listened icon if it's NOT the first listen (isFirstListen = false)
            holder.listenedIcon.visibility =
                if (!review.isFirstListen) View.VISIBLE else View.GONE

            // Set listened icon color to match your theme
            ImageViewCompat.setImageTintList(
                holder.listenedIcon,
                android.content.res.ColorStateList.valueOf(activeColor)
            )

            holder.itemView.setOnClickListener {
                onReviewClick?.invoke(review)
            }
        }

        override fun getItemCount(): Int = reviews.size

        fun submitList(newReviews: List<Review>) {
            reviews = newReviews
            notifyDataSetChanged()
        }
    }
}