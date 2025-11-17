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
import student.projects.musicreviewapp.auth.ListManager
import student.projects.musicreviewapp.auth.ReviewManager
import student.projects.musicreviewapp.models.Music
import student.projects.musicreviewapp.models.Review
import student.projects.musicreviewapp.models.UserList
import student.projects.musicreviewapp.network.SpotifyApiService

class HomeFragment : Fragment() {

    private lateinit var authManager: AuthManager
    private lateinit var spotifyApiService: SpotifyApiService
    private lateinit var reviewManager: ReviewManager
    private lateinit var listManager: ListManager

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
    private var newReleasesAlbums = listOf<Music>()
    private var trendingAlbums = listOf<Music>()
    private var popularWeekAlbums = listOf<Music>()

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
        reviewManager = ReviewManager(requireContext())
        listManager = ListManager(requireContext())
        return inflater.inflate(R.layout.fragment_home_signed_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupNavigationBar(view)
        setupTabBar(view)
        setupContentSections(view)

        // Set initial state - Albums tab active
        setActiveTab(Tab.ALBUMS)
        loadActiveTabData()
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
            Log.e("HomeFragment", "Error initializing views: ${e.message}")
            throw e
        }
    }

    private fun setupTabBar(view: View) {
        tabAlbums.setOnClickListener {
            setActiveTab(Tab.ALBUMS)
            loadActiveTabData()
        }
        tabReviews.setOnClickListener {
            setActiveTab(Tab.REVIEWS)
            loadActiveTabData()
        }
        tabLists.setOnClickListener {
            setActiveTab(Tab.LISTS)
            loadActiveTabData()
        }
    }

    private fun loadActiveTabData() {
        when {
            albumsContent.visibility == View.VISIBLE -> {
                if (!isDataLoaded || (System.currentTimeMillis() - cacheTimestamp) >= CACHE_DURATION) {
                    loadSpotifyData()
                    isDataLoaded = true
                    cacheTimestamp = System.currentTimeMillis()
                } else {
                    populateAlbumSections()
                }
            }
            reviewsContent.visibility == View.VISIBLE -> {
                loadPopularReviews()
            }
            listsContent.visibility == View.VISIBLE -> {
                loadPopularLists()
            }
        }
    }

    // Load reviews sorted by likes
    private fun loadPopularReviews() {
        val popularReviews = reviewManager.getPopularReviews()
        updateReviewsContent(popularReviews)
    }

    // Load lists sorted by likes
    private fun loadPopularLists() {
        val popularLists = listManager.getPopularLists()
        updateListsContent(popularLists)
    }

    // Update reviews content with real data
    private fun updateReviewsContent(reviews: List<Review>) {
        val reviewsContainer = reviewsContent.findViewById<LinearLayout>(R.id.reviews_container)
        reviewsContainer?.removeAllViews()

        if (reviews.isEmpty()) {
            val emptyView = TextView(requireContext()).apply {
                text = "No reviews yet"
                textSize = 16f
                setTextColor(resources.getColor(android.R.color.darker_gray))
                gravity = android.view.Gravity.CENTER
                setPadding(0, 50, 0, 50)
            }
            reviewsContainer?.addView(emptyView)
            return
        }

        reviews.take(5).forEach { review ->
            val reviewView = createReviewView(review)
            reviewsContainer?.addView(reviewView)

            // Add divider except for last item
            if (review != reviews.last()) {
                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                    ).apply {
                        setMargins(0, 20, 0, 20)
                    }
                    setBackgroundColor(resources.getColor(R.color.divider_gray))
                }
                reviewsContainer?.addView(divider)
            }
        }
    }

    // Create review view with real data
    private fun createReviewView(review: Review): View {
        val inflater = LayoutInflater.from(requireContext())
        val reviewView = inflater.inflate(R.layout.layout_music_review_item, null)

        // Album cover
        val albumCover = reviewView.findViewById<ImageView>(R.id.album_cover)
        if (!review.musicCoverUrl.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(review.musicCoverUrl)
                .placeholder(R.drawable.album_placeholder)
                .error(R.drawable.album_placeholder)
                .into(albumCover)
        } else {
            albumCover.setImageResource(R.drawable.album_placeholder)
        }

        // Album title and year
        reviewView.findViewById<TextView>(R.id.album_title).text = review.musicTitle
        reviewView.findViewById<TextView>(R.id.album_year).text = review.musicYear

        // Rating stars
        updateRatingStars(reviewView, review.rating)

        // Like count
        reviewView.findViewById<TextView>(R.id.like_count).text = review.likes.toString()

        // User name
        reviewView.findViewById<TextView>(R.id.user_name).text = review.userName

        // Review content
        reviewView.findViewById<TextView>(R.id.review_content).text = review.content

        // Click listener
        reviewView.setOnClickListener {
            navigateToReviewDetail(review)
        }

        return reviewView
    }

    private fun updateRatingStars(reviewView: View, rating: Int) {
        val stars = listOf(
            reviewView.findViewById<ImageView>(R.id.star1),
            reviewView.findViewById<ImageView>(R.id.star2),
            reviewView.findViewById<ImageView>(R.id.star3),
            reviewView.findViewById<ImageView>(R.id.star4),
            reviewView.findViewById<ImageView>(R.id.star5)
        )

        stars.forEachIndexed { index, star ->
            star.setImageResource(
                if (index < rating) R.drawable.ic_star_purple else R.drawable.ic_star
            )
        }
    }

    // Update lists content with real data
    private fun updateListsContent(lists: List<UserList>) {
        // Since your lists layout uses hardcoded content, we'll create dynamic views
        listsContent.removeAllViews()

        if (lists.isEmpty()) {
            val emptyView = TextView(requireContext()).apply {
                text = "No lists yet"
                textSize = 16f
                setTextColor(resources.getColor(android.R.color.darker_gray))
                gravity = android.view.Gravity.CENTER
                setPadding(0, 50, 0, 50)
            }
            listsContent.addView(emptyView)
            return
        }

        lists.forEach { list ->
            val listView = createListView(list)
            listsContent.addView(listView)
        }
    }

    private fun createListView(list: UserList): View {
        val inflater = LayoutInflater.from(requireContext())
        val listView = inflater.inflate(R.layout.layout_list_item, null)

        // List title and creator
        listView.findViewById<TextView>(R.id.list_title).text = list.name
        listView.findViewById<TextView>(R.id.list_creator).text = list.creator

        // List description
        listView.findViewById<TextView>(R.id.list_description).text = list.description

        // Album covers
        val albumsContainer = listView.findViewById<LinearLayout>(R.id.albums_container)
        albumsContainer.removeAllViews()

        list.albums.take(6).forEach { album ->
            val albumCover = ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(110), dpToPx(160)).apply {
                    marginEnd = dpToPx(10)
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                background = resources.getDrawable(R.drawable.rounded_album, null)

                if (album.coverImage.isNotEmpty()) {
                    Glide.with(requireContext())
                        .load(album.coverImage)
                        .placeholder(R.drawable.album_placeholder)
                        .error(R.drawable.album_placeholder)
                        .into(this)
                } else {
                    setImageResource(R.drawable.album_placeholder)
                }
            }
            albumsContainer.addView(albumCover)
        }

        // Click listener
        listView.setOnClickListener {
            navigateToListDetail(list)
        }

        return listView
    }

    private fun navigateToReviewDetail(review: Review) {
        val bundle = Bundle().apply {
            putParcelable("review", review)
        }
        findNavController().navigate(R.id.action_homeFragment_to_reviewDetailFragment, bundle)
    }

    private fun navigateToListDetail(list: UserList) {
        val bundle = Bundle().apply {
            putParcelable("list", list)
        }
        findNavController().navigate(R.id.action_homeFragment_to_listDetailFragment, bundle)
    }

    // Existing methods from your original HomeFragment
    private fun loadSpotifyData() {
        Log.d("HomeFragment", "Starting to load Spotify data...")

        spotifyApiService.getPopularThisWeek(object : SpotifyApiService.SpotifyCallback<List<Music>> {
            override fun onSuccess(result: List<Music>) {
                Log.d("HomeFragment", "✅ Popular this week loaded: ${result.size} items")
                popularWeekAlbums = result
                loadFriendsAlbums()
            }
            override fun onError(error: String) {
                Log.e("HomeFragment", "❌ Failed to load popular this week: $error")
                popularWeekAlbums = getPopularMockData()
                loadFriendsAlbums()
            }
        })
    }

    private fun loadFriendsAlbums() {
        Log.d("HomeFragment", "🔄 Calling getFriendsAlbums() for From Friends section")

        spotifyApiService.getFriendsAlbums(object : SpotifyApiService.SpotifyCallback<List<Music>> {
            override fun onSuccess(result: List<Music>) {
                Log.d("HomeFragment", "✅ Friends albums loaded: ${result.size} items")
                newReleasesAlbums = result
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
        spotifyApiService.getSimpleTrendingMusic(object : SpotifyApiService.SpotifyCallback<List<Music>> {
            override fun onSuccess(result: List<Music>) {
                Log.d("HomeFragment", "✅ Simple trending loaded: ${result.size} items")
                trendingAlbums = result
                    .distinctBy { it.album }
                    .sortedByDescending { it.averageRating }
                    .take(8)
                populateAlbumSections()
                showToast("Loaded trending music")
                cacheTimestamp = System.currentTimeMillis()
            }
            override fun onError(error: String) {
                Log.e("HomeFragment", "❌ Simple trending failed: $error")
                loadArtistBasedTrending()
            }
        })
    }

    private fun loadArtistBasedTrending() {
        val popularArtists = listOf("Taylor Swift", "Drake", "Bad Bunny", "The Weeknd", "Ed Sheeran")
        val randomArtist = popularArtists.random()

        spotifyApiService.searchMusic(randomArtist, object : SpotifyApiService.SpotifyCallback<List<Music>> {
            override fun onSuccess(result: List<Music>) {
                Log.d("HomeFragment", "✅ Artist-based trending loaded: ${result.size} items")
                trendingAlbums = result.distinctBy { it.album }.take(8)
                populateAlbumSections()
                showToast("Loaded popular music")
                cacheTimestamp = System.currentTimeMillis()
            }
            override fun onError(error: String) {
                Log.e("HomeFragment", "❌ All trending methods failed: $error")
                trendingAlbums = getTrendingMockData()
                populateAlbumSections()
                showToast("Using sample trending data")
                cacheTimestamp = System.currentTimeMillis()
            }
        })
    }

    private fun getPopularMockData(): List<Music> {
        return listOf(
            Music(
                id = "new1", title = "New Album 2024", artist = "Various Artists",
                album = "New Album 2024", releaseYear = 2024, genre = "Various",
                coverImage = "https://via.placeholder.com/300/8B7D9E/FFFFFF?text=New+2024",
                averageRating = 4.2, reviewCount = 50
            ),
            Music(
                id = "new2", title = "Fresh Sounds", artist = "New Artist",
                album = "Fresh Sounds", releaseYear = 2024, genre = "Pop",
                coverImage = "https://via.placeholder.com/300/5D4A6F/FFFFFF?text=Fresh",
                averageRating = 4.0, reviewCount = 35
            )
        )
    }

    private fun getFriendsMockData(): List<Music> {
        return listOf(
            Music(
                id = "friend1", title = "Midnights", artist = "Taylor Swift",
                album = "Midnights", releaseYear = 2022, genre = "Pop",
                coverImage = "https://via.placeholder.com/300/8B7D9E/FFFFFF?text=Midnights",
                averageRating = 4.8, reviewCount = 280
            ),
            Music(
                id = "friend2", title = "Happier Than Ever", artist = "Billie Eilish",
                album = "Happier Than Ever", releaseYear = 2021, genre = "Pop",
                coverImage = "https://via.placeholder.com/300/5D4A6F/FFFFFF?text=Billie",
                averageRating = 4.6, reviewCount = 220
            )
        )
    }

    private fun getTrendingMockData(): List<Music> {
        return listOf(
            Music(
                id = "trend1", title = "UTOPIA", artist = "Travis Scott",
                album = "UTOPIA", releaseYear = 2023, genre = "Hip-Hop",
                coverImage = "https://via.placeholder.com/300/8B7D9E/FFFFFF?text=UTOPIA",
                averageRating = 4.8, reviewCount = 250
            ),
            Music(
                id = "trend2", title = "Midnights", artist = "Taylor Swift",
                album = "Midnights", releaseYear = 2022, genre = "Pop",
                coverImage = "https://via.placeholder.com/300/5D4A6F/FFFFFF?text=Midnights",
                averageRating = 4.9, reviewCount = 300
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
            dpToPx(280), // Call as regular method
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
        updateSectionTitles()
        popularWeekContainer.removeAllViews()
        newFriendsContainer.removeAllViews()
        trendingContainer.removeAllViews()

        Log.d("HomeFragment", "Popular this week: ${popularWeekAlbums.size}, From friends: ${newReleasesAlbums.size}, Trending: ${trendingAlbums.size}")

        if (popularWeekAlbums.isNotEmpty()) {
            popularWeekAlbums.take(4).forEach { music ->
                popularWeekContainer.addView(createAlbumView(music))
            }
        }

        if (newReleasesAlbums.isNotEmpty()) {
            newReleasesAlbums.take(4).forEach { music ->
                newFriendsContainer.addView(createAlbumView(music))
            }
        }

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

            if (music.coverImage.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(music.coverImage)
                    .placeholder(R.drawable.album_placeholder)
                    .error(R.drawable.album_placeholder)
                    .into(this)
            } else {
                setImageResource(R.drawable.album_placeholder)
            }

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