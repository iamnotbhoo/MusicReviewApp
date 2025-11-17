package student.projects.musicreviewapp.ui.home

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.util.Log
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.auth.AuthManager
import student.projects.musicreviewapp.models.Music
import student.projects.musicreviewapp.network.SpotifyApiService

class HomeFragment : Fragment() {

    private lateinit var authManager: AuthManager
    private lateinit var spotifyApiService: SpotifyApiService

    // Tab buttons
    private lateinit var tabAlbums: Button
    private lateinit var tabReviews: Button
    private lateinit var tabLists: Button

    // Indicators
    private lateinit var albumsIndicator: View
    private lateinit var reviewsIndicator: View
    private lateinit var listsIndicator: View

    // Content sections
    private lateinit var albumsContent: LinearLayout
    private lateinit var reviewsContent: LinearLayout
    private lateinit var listsContent: LinearLayout

    // Section containers
    private lateinit var popularWeekContainer: LinearLayout
    private lateinit var newFriendsContainer: LinearLayout
    private lateinit var trendingContainer: LinearLayout

    // Section titles
    private lateinit var popularWeekTitle: TextView
    private lateinit var newFriendsTitle: TextView
    private lateinit var trendingTitle: TextView

    private var menuPopup: PopupWindow? = null

    // Spotify data
    private var newReleasesAlbums = listOf<Music>() // For "From friends" and "Popular this week"
    private var trendingAlbums = listOf<Music>() // For "Trending now"
    private var popularWeekAlbums = listOf<Music>() // For "Popular this week" section

    // Cache variables
    private var isDataLoaded = false
    private var cacheTimestamp: Long = 0
    private val CACHE_DURATION = 30 * 60 * 1000 // 30 minutes

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

        // Initialize all views
        initializeViews(view)

        setupNavigationBar(view)
        setupTabBar(view)
        setupContentSections(view)

        // Check if we have cached data, otherwise load new data
        if (isDataLoaded && (System.currentTimeMillis() - cacheTimestamp) < CACHE_DURATION) {
            // Use cached data
            populateAlbumSections()
            Log.d("HomeFragment", "🔄 Using cached data")
        } else {
            // Load new Spotify data
            loadSpotifyData()
            isDataLoaded = true
            cacheTimestamp = System.currentTimeMillis()
        }

        // Set initial state - Albums tab active
        setActiveTab(Tab.ALBUMS)
    }

    private fun initializeViews(view: View) {
        try {
            // Initialize tab buttons
            tabAlbums = view.findViewById(R.id.tab_albums)
            tabReviews = view.findViewById(R.id.tab_reviews)
            tabLists = view.findViewById(R.id.tab_lists)

            // Initialize indicators
            albumsIndicator = view.findViewById(R.id.albums_indicator)
            reviewsIndicator = view.findViewById(R.id.reviews_indicator)
            listsIndicator = view.findViewById(R.id.lists_indicator)

            // Initialize content sections
            albumsContent = view.findViewById(R.id.albums_content)
            reviewsContent = view.findViewById(R.id.reviews_content)
            listsContent = view.findViewById(R.id.lists_content)

            // Initialize section containers
            popularWeekContainer = view.findViewById(R.id.popular_week_container)
            newFriendsContainer = view.findViewById(R.id.new_friends_container)
            trendingContainer = view.findViewById(R.id.popular_friends_container)

            // Initialize section titles
            popularWeekTitle = view.findViewById(R.id.popular_week_title)
            newFriendsTitle = view.findViewById(R.id.new_friends_title)
            trendingTitle = view.findViewById(R.id.popular_friends_title)
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error initializing views: ${e.message}")
            throw e
        }
    }

    private fun loadSpotifyData() {
        android.util.Log.d("HomeFragment", "Starting to load Spotify data...")

        // Load popular albums for "Popular this week" section
        spotifyApiService.getPopularThisWeek(object : SpotifyApiService.SpotifyCallback<List<Music>> {
            override fun onSuccess(result: List<Music>) {
                android.util.Log.d("HomeFragment", "✅ Popular this week loaded: ${result.size} items")
                popularWeekAlbums = result
                loadFriendsAlbums() // This should call getFriendsAlbums, NOT getNewReleases
            }
            override fun onError(error: String) {
                android.util.Log.e("HomeFragment", "❌ Failed to load popular this week: $error")
                popularWeekAlbums = getPopularMockData()
                loadFriendsAlbums()
            }
        })
    }

    private fun loadFriendsAlbums() {
        Log.d("HomeFragment", "🔄 Calling getFriendsAlbums() for From Friends section")

        // This calls getFriendsAlbums for the "From friends" section
        spotifyApiService.getFriendsAlbums(object : SpotifyApiService.SpotifyCallback<List<Music>> {
            override fun onSuccess(result: List<Music>) {
                Log.d("HomeFragment", "✅ Friends albums loaded: ${result.size} items")
                newReleasesAlbums = result // This variable is used for "From friends" section
                loadSimpleTrendingData()
            }
            override fun onError(error: String) {
                Log.e("HomeFragment", "❌ Failed to load friends albums: $error")
                newReleasesAlbums = getFriendsMockData()
                loadSimpleTrendingData()
            }
        })
    }

    private fun loadSimpleTrendingData() {
        // Use the simple trending approach that's more likely to work
        spotifyApiService.getSimpleTrendingMusic(object : SpotifyApiService.SpotifyCallback<List<Music>> {
            override fun onSuccess(result: List<Music>) {
                android.util.Log.d("HomeFragment", "✅ Simple trending loaded: ${result.size} items")
                // Filter to get unique albums and take the most popular ones
                trendingAlbums = result
                    .distinctBy { it.album }
                    .sortedByDescending { it.averageRating }
                    .take(8)
                populateAlbumSections()
                showToast("Loaded trending music")

                // Set cache timestamp when all data is loaded
                cacheTimestamp = System.currentTimeMillis()
            }
            override fun onError(error: String) {
                android.util.Log.e("HomeFragment", "❌ Simple trending failed: $error")
                // Try one more fallback - search for specific popular artists
                loadArtistBasedTrending()
            }
        })
    }

    private fun loadArtistBasedTrending() {
        // Search for specific popular artists as final fallback
        val popularArtists = listOf("Taylor Swift", "Drake", "Bad Bunny", "The Weeknd", "Ed Sheeran")
        val randomArtist = popularArtists.random()

        spotifyApiService.searchMusic(randomArtist, object : SpotifyApiService.SpotifyCallback<List<Music>> {
            override fun onSuccess(result: List<Music>) {
                android.util.Log.d("HomeFragment", "✅ Artist-based trending loaded: ${result.size} items")
                trendingAlbums = result
                    .distinctBy { it.album }
                    .take(8)
                populateAlbumSections()
                showToast("Loaded popular music")

                // Set cache timestamp when all data is loaded
                cacheTimestamp = System.currentTimeMillis()
            }
            override fun onError(error: String) {
                android.util.Log.e("HomeFragment", "❌ All trending methods failed: $error")
                trendingAlbums = getTrendingMockData()
                populateAlbumSections()
                showToast("Using sample trending data")

                // Set cache timestamp when all data is loaded (even with mock data)
                cacheTimestamp = System.currentTimeMillis()
            }
        })
    }

    private fun getPopularMockData(): List<Music> {
        return listOf(
            Music(
                id = "new1",
                title = "New Album 2024",
                artist = "Various Artists",
                album = "New Album 2024",
                releaseYear = 2024,
                genre = "Various",
                coverImage = "https://via.placeholder.com/300/8B7D9E/FFFFFF?text=New+2024",
                averageRating = 4.2,
                reviewCount = 50
            ),
            Music(
                id = "new2",
                title = "Fresh Sounds",
                artist = "New Artist",
                album = "Fresh Sounds",
                releaseYear = 2024,
                genre = "Pop",
                coverImage = "https://via.placeholder.com/300/5D4A6F/FFFFFF?text=Fresh",
                averageRating = 4.0,
                reviewCount = 35
            ),
            Music(
                id = "new3",
                title = "Latest Hits",
                artist = "Chart Toppers",
                album = "Latest Hits",
                releaseYear = 2024,
                genre = "Various",
                coverImage = "https://via.placeholder.com/300/8B7D9E/FFFFFF?text=Latest",
                averageRating = 4.3,
                reviewCount = 42
            ),
            Music(
                id = "new4",
                title = "Brand New",
                artist = "Upcoming Star",
                album = "Brand New",
                releaseYear = 2024,
                genre = "R&B",
                coverImage = "https://via.placeholder.com/300/5D4A6F/FFFFFF?text=Brand+New",
                averageRating = 4.1,
                reviewCount = 28
            )
        )
    }

    private fun getFriendsMockData(): List<Music> {
        return listOf(
            Music(
                id = "friend1",
                title = "Midnights",
                artist = "Taylor Swift",
                album = "Midnights",
                releaseYear = 2022,
                genre = "Pop",
                coverImage = "https://via.placeholder.com/300/8B7D9E/FFFFFF?text=Midnights",
                averageRating = 4.8,
                reviewCount = 280
            ),
            Music(
                id = "friend2",
                title = "Happier Than Ever",
                artist = "Billie Eilish",
                album = "Happier Than Ever",
                releaseYear = 2021,
                genre = "Pop",
                coverImage = "https://via.placeholder.com/300/5D4A6F/FFFFFF?text=Billie",
                averageRating = 4.6,
                reviewCount = 220
            ),
            Music(
                id = "friend3",
                title = "Future Nostalgia",
                artist = "Dua Lipa",
                album = "Future Nostalgia",
                releaseYear = 2020,
                genre = "Pop",
                coverImage = "https://via.placeholder.com/300/8B7D9E/FFFFFF?text=Dua+Lipa",
                averageRating = 4.7,
                reviewCount = 240
            ),
            Music(
                id = "friend4",
                title = "Fine Line",
                artist = "Harry Styles",
                album = "Fine Line",
                releaseYear = 2019,
                genre = "Pop",
                coverImage = "https://via.placeholder.com/300/5D4A6F/FFFFFF?text=Fine+Line",
                averageRating = 4.5,
                reviewCount = 200
            )
        )
    }

    private fun getTrendingMockData(): List<Music> {
        return listOf(
            Music(
                id = "trend1",
                title = "UTOPIA",
                artist = "Travis Scott",
                album = "UTOPIA",
                releaseYear = 2023,
                genre = "Hip-Hop",
                coverImage = "https://via.placeholder.com/300/8B7D9E/FFFFFF?text=UTOPIA",
                averageRating = 4.8,
                reviewCount = 250
            ),
            Music(
                id = "trend2",
                title = "Midnights",
                artist = "Taylor Swift",
                album = "Midnights",
                releaseYear = 2022,
                genre = "Pop",
                coverImage = "https://via.placeholder.com/300/5D4A6F/FFFFFF?text=Midnights",
                averageRating = 4.9,
                reviewCount = 300
            ),
            Music(
                id = "trend3",
                title = "Un Verano Sin Ti",
                artist = "Bad Bunny",
                album = "Un Verano Sin Ti",
                releaseYear = 2022,
                genre = "Reggaeton",
                coverImage = "https://via.placeholder.com/300/8B7D9E/FFFFFF?text=Un+Verano",
                averageRating = 4.7,
                reviewCount = 280
            ),
            Music(
                id = "trend4",
                title = "DAMN",
                artist = "Kendrick Lamar",
                album = "DAMN",
                releaseYear = 2017,
                genre = "Hip-Hop",
                coverImage = "https://via.placeholder.com/300/5D4A6F/FFFFFF?text=DAMN",
                averageRating = 4.9,
                reviewCount = 320
            )
        )
    }

    private fun setupNavigationBar(view: View) {
        val menuButton = view.findViewById<ImageButton>(R.id.menu_button)
        menuButton.setOnClickListener {
            showNavigationMenu(menuButton)
        }
    }

    private fun showNavigationMenu(anchorView: View) {
        val menuView = LayoutInflater.from(requireContext()).inflate(R.layout.navigation_drawer, null)
        menuView.findViewById<View>(R.id.menu_activity).visibility = View.GONE

        menuPopup = PopupWindow(
            menuView,
            280.dpToPx(requireContext()),
            ViewGroup.LayoutParams.MATCH_PARENT,
            true
        ).apply {
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            elevation = 16f
            animationStyle = R.style.MenuAnimation
            showAtLocation(anchorView, Gravity.START, 0, 0)
        }
        setupMenuClickListeners(menuView)
    }

    private fun Int.dpToPx(context: android.content.Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    private fun setupMenuClickListeners(menuView: View) {
        menuView.findViewById<View>(R.id.menu_popular).setOnClickListener {
            menuPopup?.dismiss()
        }
        menuView.findViewById<View>(R.id.menu_playlist).setOnClickListener {
            menuPopup?.dismiss()
            findNavController().navigate(R.id.action_homeFragment_to_playlistFragment)
        }
        menuView.findViewById<View>(R.id.menu_lists).setOnClickListener {
            menuPopup?.dismiss()
            findNavController().navigate(R.id.action_homeFragment_to_userListsFragment)
        }
        menuView.findViewById<View>(R.id.menu_diary).setOnClickListener {
            menuPopup?.dismiss()
            findNavController().navigate(R.id.action_homeFragment_to_diaryFragment)
        }
        menuView.findViewById<View>(R.id.menu_reviews).setOnClickListener {
            menuPopup?.dismiss()
            findNavController().navigate(R.id.action_homeFragment_to_userReviewsFragment)
        }
        menuView.findViewById<View>(R.id.menu_sign_out).setOnClickListener {
            menuPopup?.dismiss()
            performSignOut()
        }
    }

    private fun performSignOut() {
        authManager.logout()
        showToast("Signed out successfully")
        findNavController().navigate(R.id.action_homeFragment_to_welcomeFragment)
    }

    private fun setupTabBar(view: View) {
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
        // Content sections are already initialized in initializeViews()
    }

    private fun setActiveTab(activeTab: Tab) {
        albumsIndicator.visibility = View.GONE
        reviewsIndicator.visibility = View.GONE
        listsIndicator.visibility = View.GONE

        albumsContent.visibility = View.GONE
        reviewsContent.visibility = View.GONE
        listsContent.visibility = View.GONE

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
        // Update section titles
        updateSectionTitles()

        // Clear existing views
        popularWeekContainer.removeAllViews()
        newFriendsContainer.removeAllViews()
        trendingContainer.removeAllViews()

        // Debug logging
        android.util.Log.d("HomeFragment", "Popular this week: ${popularWeekAlbums.size}, From friends: ${newReleasesAlbums.size}, Trending: ${trendingAlbums.size}")

        // Populate "Popular this week" with popular albums
        if (popularWeekAlbums.isNotEmpty()) {
            popularWeekAlbums.take(4).forEach { music ->
                popularWeekContainer.addView(createAlbumView(music))
            }
        }

        // Populate "From friends" with friends albums
        if (newReleasesAlbums.isNotEmpty()) {
            newReleasesAlbums.take(4).forEach { music ->
                newFriendsContainer.addView(createAlbumView(music))
            }
        }

        // Populate "Trending now" with trending albums
        if (trendingAlbums.isNotEmpty()) {
            trendingAlbums.take(4).forEach { music ->
                trendingContainer.addView(createAlbumView(music))
            }
        }
    }

    private fun updateSectionTitles() {
        popularWeekTitle.text = "Popular this week"
        newFriendsTitle.text = "From friends"
        trendingTitle.text = "Trending now"
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