package student.projects.musicreviewapp.ui.home

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.auth.AuthManager
import student.projects.musicreviewapp.models.Music

class HomeFragment : Fragment() {

    private lateinit var authManager: AuthManager
    private lateinit var albumsIndicator: View
    private lateinit var reviewsIndicator: View
    private lateinit var listsIndicator: View
    private lateinit var albumsContent: LinearLayout
    private lateinit var reviewsContent: LinearLayout
    private lateinit var listsContent: LinearLayout

    private var menuPopup: PopupWindow? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        authManager = AuthManager(requireContext())
        return inflater.inflate(R.layout.fragment_home_signed_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupNavigationBar(view)
        setupTabBar(view)
        setupContentSections(view)
        populateAlbumSections(view)

        // Set initial state - Albums tab active
        setActiveTab(Tab.ALBUMS)
    }

    private fun setupNavigationBar(view: View) {
        val menuButton = view.findViewById<ImageButton>(R.id.menu_button)

        menuButton.setOnClickListener {
            showNavigationMenu(menuButton)
        }
    }

    private fun showNavigationMenu(anchorView: View) {
        // Inflate the menu layout
        val menuView = LayoutInflater.from(requireContext()).inflate(R.layout.navigation_drawer, null)

        // Create the popup window
        menuPopup = PopupWindow(
            menuView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            true
        ).apply {
            // Set background to make it look like a proper drawer
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            elevation = 16f
            animationStyle = R.style.MenuAnimation

            // Show the menu
            showAsDropDown(anchorView, -anchorView.width, -anchorView.height, Gravity.START)
        }

        // Set up menu item click listeners
        setupMenuClickListeners(menuView)
    }

    private fun setupMenuClickListeners(menuView: View) {
        // Popular - go to home (current page)
        menuView.findViewById<View>(R.id.menu_popular).setOnClickListener {
            // Already on home page, just close menu
            menuPopup?.dismiss()
        }

        // Playlist
        menuView.findViewById<View>(R.id.menu_playlist).setOnClickListener {
            menuPopup?.dismiss()
            showToast("Playlist feature coming soon!")
            // Navigate to playlist page when created
            // findNavController().navigate(R.id.playlistFragment)
        }

        // Lists - switch to lists tab
        menuView.findViewById<View>(R.id.menu_lists).setOnClickListener {
            menuPopup?.dismiss()
            setActiveTab(Tab.LISTS)
        }

        // Diary
        menuView.findViewById<View>(R.id.menu_diary).setOnClickListener {
            menuPopup?.dismiss()
            showToast("Diary feature coming soon!")
            // Navigate to diary page when created
            // findNavController().navigate(R.id.diaryFragment)
        }

        // Reviews - switch to reviews tab
        menuView.findViewById<View>(R.id.menu_reviews).setOnClickListener {
            menuPopup?.dismiss()
            setActiveTab(Tab.REVIEWS)
        }

        // Activity
        menuView.findViewById<View>(R.id.menu_activity).setOnClickListener {
            menuPopup?.dismiss()
            showToast("Activity feature coming soon!")
            // Navigate to activity page when created
            // findNavController().navigate(R.id.activityFragment)
        }

        // Settings
        menuView.findViewById<View>(R.id.menu_settings).setOnClickListener {
            menuPopup?.dismiss()
            showToast("Settings feature coming soon!")
            // Navigate to settings page when created
            // findNavController().navigate(R.id.settingsFragment)
        }

        // Sign Out
        menuView.findViewById<View>(R.id.menu_sign_out).setOnClickListener {
            menuPopup?.dismiss()
            performSignOut()
        }
    }

    private fun performSignOut() {
        authManager.logout()
        showToast("Signed out successfully")

        // Navigate back to welcome screen
        findNavController().navigate(R.id.action_homeFragment_to_welcomeFragment)
    }

    private fun setupTabBar(view: View) {
        val tabAlbums = view.findViewById<View>(R.id.tab_albums)
        val tabReviews = view.findViewById<View>(R.id.tab_reviews)
        val tabLists = view.findViewById<View>(R.id.tab_lists)

        // Initialize indicators
        albumsIndicator = view.findViewById(R.id.albums_indicator)
        reviewsIndicator = view.findViewById(R.id.reviews_indicator)
        listsIndicator = view.findViewById(R.id.lists_indicator)

        tabAlbums.setOnClickListener {
            setActiveTab(Tab.ALBUMS)
        }

        tabReviews.setOnClickListener {
            setActiveTab(Tab.REVIEWS)
        }

        tabLists.setOnClickListener {
            setActiveTab(Tab.LISTS)
        }
    }

    private fun setupContentSections(view: View) {
        // Initialize content sections
        albumsContent = view.findViewById(R.id.albums_content)
        reviewsContent = view.findViewById(R.id.reviews_content)
        listsContent = view.findViewById(R.id.lists_content)

        // Remove placeholder setup since we're using actual layouts now
        // setupReviewsContent() and setupListsContent() are no longer needed
    }

    private fun setActiveTab(activeTab: Tab) {
        // Reset all indicators
        albumsIndicator.visibility = View.GONE
        reviewsIndicator.visibility = View.GONE
        listsIndicator.visibility = View.GONE

        // Hide all content sections
        albumsContent.visibility = View.GONE
        reviewsContent.visibility = View.GONE
        listsContent.visibility = View.GONE

        // Set active indicator and show corresponding content
        when (activeTab) {
            Tab.ALBUMS -> {
                albumsIndicator.visibility = View.VISIBLE
                albumsContent.visibility = View.VISIBLE
            }
            Tab.REVIEWS -> {
                reviewsIndicator.visibility = View.VISIBLE
                reviewsContent.visibility = View.VISIBLE
            }
            Tab.LISTS -> {
                listsIndicator.visibility = View.VISIBLE
                listsContent.visibility = View.VISIBLE
            }
        }
    }

    // Remove these methods since we're using actual layouts now
    // private fun setupReviewsContent() { ... }
    // private fun setupListsContent() { ... }

    private fun populateAlbumSections(view: View) {
        val popularWeekContainer = view.findViewById<LinearLayout>(R.id.popular_week_container)
        val newFriendsContainer = view.findViewById<LinearLayout>(R.id.new_friends_container)
        val popularFriendsContainer = view.findViewById<LinearLayout>(R.id.popular_friends_container)

        val popularMusic = getPopularMusic()

        // Clear existing views
        popularWeekContainer.removeAllViews()
        newFriendsContainer.removeAllViews()
        popularFriendsContainer.removeAllViews()

        // Populate "Popular this week"
        popularMusic.take(4).forEach { music ->
            popularWeekContainer.addView(createAlbumView(music))
        }

        // Populate "New from friends"
        popularMusic.reversed().take(4).forEach { music ->
            newFriendsContainer.addView(createAlbumView(music))
        }

        // Populate "Popular with friends"
        popularMusic.shuffled().take(4).forEach { music ->
            popularFriendsContainer.addView(createAlbumView(music))
        }
    }

    private fun createAlbumView(music: Music): View {
        return ImageView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(110),
                dpToPx(160)
            ).apply { marginEnd = dpToPx(10) }

            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageResource(R.drawable.rounded_album)  // placeholder
            clipToOutline = true
            background = resources.getDrawable(R.drawable.rounded_album, null)

            setOnClickListener { showAlbumDetails(music) }
        }
    }


    private fun showAlbumDetails(music: Music) {
        val message = "${music.title}\nby ${music.artist}\n${music.album} (${music.releaseYear})\n" +
                "Rating: ${music.averageRating}/5.0 (${music.reviewCount} reviews)"

        android.app.AlertDialog.Builder(requireContext())
            .setTitle(music.title)
            .setMessage(message)
            .setPositiveButton("Rate") { _, _ ->
                showRatingDialog(music)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showRatingDialog(music: Music) {
        val ratingOptions = arrayOf(
            "⭐️ (1 Star)",
            "⭐️⭐️ (2 Stars)",
            "⭐️⭐️⭐️ (3 Stars)",
            "⭐️⭐️⭐️⭐️ (4 Stars)",
            "⭐️⭐️⭐️⭐️⭐️ (5 Stars)"
        )

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Rate ${music.title}")
            .setMessage("How would you rate ${music.title} by ${music.artist}?")
            .setItems(ratingOptions) { dialog, which ->
                val stars = which + 1
                showToast("Rated ${music.title} $stars stars!")
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun getRandomColor(): Int {
        val colors = listOf(
            0xFF6B4E71.toInt(), // Purple
            0xFF4A5B6A.toInt(), // Blue grey
            0xFF8B6F47.toInt(), // Brown
            0xFF5A7C5A.toInt(), // Green
            0xFF8B5A5A.toInt(), // Red
            0xFF5A5A8B.toInt()  // Dark blue
        )
        return colors.random()
    }

    private fun getPopularMusic(): List<Music> {
        return listOf(
            Music(
                id = "1",
                title = "Blinding Lights",
                artist = "The Weeknd",
                album = "After Hours",
                releaseYear = 2020,
                genre = "Synth-pop",
                coverImage = "",
                averageRating = 4.2,
                reviewCount = 150
            ),
            Music(
                id = "2",
                title = "Flowers",
                artist = "Miley Cyrus",
                album = "Endless Summer Vacation",
                releaseYear = 2023,
                genre = "Pop",
                coverImage = "",
                averageRating = 4.0,
                reviewCount = 120
            ),
            Music(
                id = "3",
                title = "Anti-Hero",
                artist = "Taylor Swift",
                album = "Midnights",
                releaseYear = 2022,
                genre = "Pop",
                coverImage = "",
                averageRating = 4.5,
                reviewCount = 200
            ),
            Music(
                id = "4",
                title = "As It Was",
                artist = "Harry Styles",
                album = "Harry's House",
                releaseYear = 2022,
                genre = "Pop Rock",
                coverImage = "",
                averageRating = 4.3,
                reviewCount = 180
            ),
            Music(
                id = "5",
                title = "Levitating",
                artist = "Dua Lipa",
                album = "Future Nostalgia",
                releaseYear = 2020,
                genre = "Disco-pop",
                coverImage = "",
                averageRating = 4.4,
                reviewCount = 190
            ),
            Music(
                id = "6",
                title = "Good 4 U",
                artist = "Olivia Rodrigo",
                album = "SOUR",
                releaseYear = 2021,
                genre = "Pop Rock",
                coverImage = "",
                averageRating = 4.6,
                reviewCount = 220
            ),
            Music(
                id = "7",
                title = "Cruel Summer",
                artist = "Taylor Swift",
                album = "Lover",
                releaseYear = 2019,
                genre = "Pop",
                coverImage = "",
                averageRating = 4.7,
                reviewCount = 240
            ),
            Music(
                id = "8",
                title = "Vampire",
                artist = "Olivia Rodrigo",
                album = "GUTS",
                releaseYear = 2023,
                genre = "Pop Rock",
                coverImage = "",
                averageRating = 4.5,
                reviewCount = 210
            )
        )
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        menuPopup?.dismiss()
    }

    enum class Tab {
        ALBUMS, REVIEWS, LISTS
    }
}