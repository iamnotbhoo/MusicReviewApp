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
import com.bumptech.glide.Glide
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.auth.AuthManager
import student.projects.musicreviewapp.models.Music
import student.projects.musicreviewapp.network.SpotifyApiService

class HomeFragment : Fragment() {

    private lateinit var authManager: AuthManager
    private lateinit var spotifyApiService: SpotifyApiService
    private lateinit var albumsIndicator: View
    private lateinit var reviewsIndicator: View
    private lateinit var listsIndicator: View
    private lateinit var albumsContent: LinearLayout
    private lateinit var reviewsContent: LinearLayout
    private lateinit var listsContent: LinearLayout

    private var menuPopup: PopupWindow? = null

    // Spotify data
    private var recommendedAlbums = listOf<Music>()
    private var popularAlbums = listOf<Music>()
    private var trendingAlbums = listOf<Music>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        authManager = AuthManager(requireContext())
        spotifyApiService = SpotifyApiService(requireContext())
        return inflater.inflate(R.layout.fragment_home_signed_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupNavigationBar(view)
        setupTabBar(view)
        setupContentSections(view)

        // Load Spotify data
        loadSpotifyData()

        // Set initial state - Albums tab active
        setActiveTab(Tab.ALBUMS)
    }

    private fun loadSpotifyData() {
        // Load recommended albums
        spotifyApiService.getRecommendedAlbums(object : SpotifyApiService.SpotifyCallback<List<Music>> {
            override fun onSuccess(result: List<Music>) {
                recommendedAlbums = result
                populateAlbumSections()
            }
            override fun onError(error: String) {
                showToast("Failed to load recommended albums")
                // Fallback to mock data
                recommendedAlbums = getPopularMusic().take(8)
                populateAlbumSections()
            }
        })

        // Load popular albums
        spotifyApiService.getPopularAlbums(object : SpotifyApiService.SpotifyCallback<List<Music>> {
            override fun onSuccess(result: List<Music>) {
                popularAlbums = result
                populateAlbumSections()
            }
            override fun onError(error: String) {
                showToast("Failed to load popular albums")
                // Fallback to mock data
                popularAlbums = getPopularMusic().shuffled().take(7)
                populateAlbumSections()
            }
        })

        // Load trending albums
        spotifyApiService.getTrendingAlbums(object : SpotifyApiService.SpotifyCallback<List<Music>> {
            override fun onSuccess(result: List<Music>) {
                trendingAlbums = result
                populateAlbumSections()
            }
            override fun onError(error: String) {
                showToast("Failed to load trending albums")
                // Fallback to mock data
                trendingAlbums = getPopularMusic().shuffled().take(7)
                populateAlbumSections()
            }
        })
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

        // REMOVED: Hide the Activity menu item
        menuView.findViewById<View>(R.id.menu_activity).visibility = View.GONE

        // Create the popup window with FIXED dimensions
        menuPopup = PopupWindow(
            menuView,
            280.dpToPx(requireContext()), // Fixed width of 280dp
            ViewGroup.LayoutParams.MATCH_PARENT, // Full height
            true
        ).apply {
            // Set background to make it look like a proper drawer
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            elevation = 16f
            animationStyle = R.style.MenuAnimation

            // Show the menu from the left edge
            showAtLocation(anchorView, Gravity.START, 0, 0)
        }

        // Set up menu item click listeners
        setupMenuClickListeners(menuView)
    }

    // Add this extension function for dp to px conversion
    private fun Int.dpToPx(context: android.content.Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    private fun setupMenuClickListeners(menuView: View) {
        // Popular - go to home (current page)
        menuView.findViewById<View>(R.id.menu_popular).setOnClickListener {
            // Already on home page, just close menu
            menuPopup?.dismiss()
        }

        // Playlist - Navigate to playlist page
        menuView.findViewById<View>(R.id.menu_playlist).setOnClickListener {
            menuPopup?.dismiss()
            findNavController().navigate(R.id.action_homeFragment_to_playlistFragment)
        }

        // Lists - Navigate to user lists page
        menuView.findViewById<View>(R.id.menu_lists).setOnClickListener {
            menuPopup?.dismiss()
            findNavController().navigate(R.id.action_homeFragment_to_userListsFragment)
        }

        // Diary - Navigate to diary page
        menuView.findViewById<View>(R.id.menu_diary).setOnClickListener {
            menuPopup?.dismiss()
            findNavController().navigate(R.id.action_homeFragment_to_diaryFragment)
        }

        // Reviews - Navigate to user reviews page
        menuView.findViewById<View>(R.id.menu_reviews).setOnClickListener {
            menuPopup?.dismiss()
            findNavController().navigate(R.id.action_homeFragment_to_userReviewsFragment)
        }

        // REMOVED: Activity menu item


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

    private fun populateAlbumSections() {
        view?.let {
            val popularWeekContainer = it.findViewById<LinearLayout>(R.id.popular_week_container)
            val newFriendsContainer = it.findViewById<LinearLayout>(R.id.new_friends_container)
            val popularFriendsContainer = it.findViewById<LinearLayout>(R.id.popular_friends_container)

            // Clear existing views
            popularWeekContainer.removeAllViews()
            newFriendsContainer.removeAllViews()
            popularFriendsContainer.removeAllViews()

            // Populate "Popular this week" with recommended albums
            recommendedAlbums.take(4).forEach { music ->
                popularWeekContainer.addView(createAlbumView(music))
            }

            // Populate "New from friends" with popular albums
            popularAlbums.take(4).forEach { music ->
                newFriendsContainer.addView(createAlbumView(music))
            }

            // Populate "Popular with friends" with trending albums
            trendingAlbums.take(4).forEach { music ->
                popularFriendsContainer.addView(createAlbumView(music))
            }
        }
    }

    private fun createAlbumView(music: Music): View {
        return ImageView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(110),
                dpToPx(160)
            ).apply { marginEnd = dpToPx(10) }

            scaleType = ImageView.ScaleType.CENTER_CROP
            clipToOutline = true
            background = resources.getDrawable(R.drawable.rounded_album, null)

            // Load album cover using Glide
            if (music.coverImage.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(music.coverImage)
                    .placeholder(R.drawable.album_placeholder)
                    .error(R.drawable.album_placeholder)
                    .into(this)
            } else {
                setImageResource(R.drawable.album_placeholder)
            }

            // Navigate to album detail page
            setOnClickListener { navigateToAlbumDetail(music) }
        }
    }

    private fun navigateToAlbumDetail(music: Music) {
        val bundle = Bundle().apply {
            putParcelable("album", music)
        }
        findNavController().navigate(R.id.action_homeFragment_to_albumDetailFragment, bundle)
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
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