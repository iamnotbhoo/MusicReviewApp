package student.projects.musicreviewapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.auth.AuthManager
import student.projects.musicreviewapp.auth.LikeManager
import student.projects.musicreviewapp.auth.ListManager
import student.projects.musicreviewapp.auth.ReviewManager
import student.projects.musicreviewapp.models.Music
import student.projects.musicreviewapp.models.Review
import student.projects.musicreviewapp.models.UserList

class LikesFragment : Fragment() {

    private lateinit var authManager: AuthManager
    private lateinit var likeManager: LikeManager
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

    // Empty states
    private lateinit var albumsEmptyState: LinearLayout
    private lateinit var reviewsEmptyState: LinearLayout
    private lateinit var listsEmptyState: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        authManager = AuthManager(requireContext())
        likeManager = LikeManager(requireContext())
        reviewManager = ReviewManager(requireContext())
        listManager = ListManager(requireContext())
        return inflater.inflate(R.layout.fragment_likes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupBackButton(view)
        setupTabBar(view)
        setupRecyclerViews(view)

        // Set initial state - Albums tab active
        setActiveTab(Tab.ALBUMS)
        loadActiveTabData()
    }

    private fun initializeViews(view: View) {
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

        // Initialize empty states
        albumsEmptyState = view.findViewById(R.id.albums_empty_state)
        reviewsEmptyState = view.findViewById(R.id.reviews_empty_state)
        listsEmptyState = view.findViewById(R.id.lists_empty_state)
    }

    private fun setupBackButton(view: View) {
        view.findViewById<ImageView>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
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

    private fun setupRecyclerViews(view: View) {
        // Albums RecyclerView - Using GridLayoutManager like your albums page
        val albumsRecycler = view.findViewById<RecyclerView>(R.id.liked_albums_recycler)
        albumsRecycler.layoutManager = GridLayoutManager(requireContext(), 2)

        // Reviews RecyclerView
        val reviewsRecycler = view.findViewById<RecyclerView>(R.id.liked_reviews_recycler)
        reviewsRecycler.layoutManager = LinearLayoutManager(requireContext())

        // Lists RecyclerView
        val listsRecycler = view.findViewById<RecyclerView>(R.id.liked_lists_recycler)
        listsRecycler.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun loadActiveTabData() {
        when {
            albumsContent.isVisible -> loadLikedAlbums()
            reviewsContent.isVisible -> loadLikedReviews()
            listsContent.isVisible -> loadLikedLists()
        }
    }

    private fun loadLikedAlbums() {
        val likedAlbums = likeManager.getLikedAlbums()
        Log.d("LikesFragment", "Loaded ${likedAlbums.size} liked albums")

        val albumsRecycler = requireView().findViewById<RecyclerView>(R.id.liked_albums_recycler)

        if (likedAlbums.isEmpty()) {
            albumsEmptyState.isVisible = true
            albumsRecycler.isVisible = false
        } else {
            albumsEmptyState.isVisible = false
            albumsRecycler.isVisible = true

            val adapter = LikedAlbumsAdapter(likedAlbums) { album ->
                navigateToAlbumDetail(album)
            }
            albumsRecycler.adapter = adapter
        }
    }

    private fun loadLikedReviews() {
        val likedReviews = likeManager.getLikedReviews()
        Log.d("LikesFragment", "Loaded ${likedReviews.size} liked reviews")

        val reviewsRecycler = requireView().findViewById<RecyclerView>(R.id.liked_reviews_recycler)

        if (likedReviews.isEmpty()) {
            reviewsEmptyState.isVisible = true
            reviewsRecycler.isVisible = false
        } else {
            reviewsEmptyState.isVisible = false
            reviewsRecycler.isVisible = true

            val adapter = LikedReviewsAdapter(likedReviews) { review ->
                navigateToReviewDetail(review)
            }
            reviewsRecycler.adapter = adapter
        }
    }

    private fun loadLikedLists() {
        val likedLists = likeManager.getLikedLists()
        Log.d("LikesFragment", "Loaded ${likedLists.size} liked lists")

        val listsRecycler = requireView().findViewById<RecyclerView>(R.id.liked_lists_recycler)

        if (likedLists.isEmpty()) {
            listsEmptyState.isVisible = true
            listsRecycler.isVisible = false
        } else {
            listsEmptyState.isVisible = false
            listsRecycler.isVisible = true

            val adapter = LikedListsAdapter(likedLists) { list ->
                navigateToListDetail(list)
            }
            listsRecycler.adapter = adapter
        }
    }

    private fun setActiveTab(activeTab: Tab) {
        // Hide all indicators
        albumsIndicator.visibility = View.GONE
        reviewsIndicator.visibility = View.GONE
        listsIndicator.visibility = View.GONE

        // Hide all content
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

    private fun navigateToAlbumDetail(music: Music) {
        val bundle = Bundle().apply {
            putParcelable("album", music)
        }
        findNavController().navigate(R.id.action_likesFragment_to_albumDetailFragment, bundle)
    }

    private fun navigateToReviewDetail(review: Review) {
        val bundle = Bundle().apply {
            putParcelable("review", review)
        }
        findNavController().navigate(R.id.action_likesFragment_to_reviewDetailFragment, bundle)
    }

    private fun navigateToListDetail(list: UserList) {
        val bundle = Bundle().apply {
            putParcelable("list", list)
        }
        findNavController().navigate(R.id.action_likesFragment_to_listDetailFragment, bundle)
    }

    enum class Tab {
        ALBUMS, REVIEWS, LISTS
    }
}

// Adapter for Liked Albums (using your album grid layout)
class LikedAlbumsAdapter(
    private val albums: List<Music>,
    private val onAlbumClick: (Music) -> Unit
) : RecyclerView.Adapter<LikedAlbumsAdapter.AlbumViewHolder>() {

    class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val albumCover: ImageView = itemView.findViewById(R.id.album_cover)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.album_cover_frame, parent, false)
        return AlbumViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = albums[position]

        // Load album cover using the specified layout
        if (album.coverImage.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(album.coverImage)
                .placeholder(R.drawable.album_placeholder)
                .error(R.drawable.album_placeholder)
                .into(holder.albumCover)
        } else {
            holder.albumCover.setImageResource(R.drawable.album_placeholder)
        }

        holder.itemView.setOnClickListener {
            onAlbumClick(album)
        }
    }

    override fun getItemCount(): Int = albums.size
}

// Adapter for Liked Reviews
class LikedReviewsAdapter(
    private val reviews: List<Review>,
    private val onReviewClick: (Review) -> Unit
) : RecyclerView.Adapter<LikedReviewsAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val albumCover: ImageView = itemView.findViewById(R.id.album_cover)
        val albumTitle: TextView = itemView.findViewById(R.id.album_title)
        val artistName: TextView = itemView.findViewById(R.id.artist_name)
        val reviewContent: TextView = itemView.findViewById(R.id.review_content)
        val userName: TextView = itemView.findViewById(R.id.user_name)
        val rating: TextView = itemView.findViewById(R.id.rating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_liked_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]

        // Load album cover
        if (!review.musicCoverUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(review.musicCoverUrl)
                .placeholder(R.drawable.album_placeholder)
                .error(R.drawable.album_placeholder)
                .into(holder.albumCover)
        } else {
            holder.albumCover.setImageResource(R.drawable.album_placeholder)
        }

        holder.albumTitle.text = review.musicTitle
        holder.artistName.text = review.musicArtist ?: "Unknown Artist"
        holder.reviewContent.text = review.content
        holder.userName.text = review.userName
        holder.rating.text = "★".repeat(review.rating)

        holder.itemView.setOnClickListener {
            onReviewClick(review)
        }
    }

    override fun getItemCount(): Int = reviews.size
}

// Adapter for Liked Lists
class LikedListsAdapter(
    private val lists: List<UserList>,
    private val onListClick: (UserList) -> Unit
) : RecyclerView.Adapter<LikedListsAdapter.ListViewHolder>() {

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val listTitle: TextView = itemView.findViewById(R.id.list_title)
        val listCreator: TextView = itemView.findViewById(R.id.list_creator)
        val listDescription: TextView = itemView.findViewById(R.id.list_description)
        val albumCoversContainer: LinearLayout = itemView.findViewById(R.id.album_covers_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_liked_list, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val list = lists[position]

        holder.listTitle.text = list.name
        holder.listCreator.text = "by ${list.creator}"
        holder.listDescription.text = list.description

        // Load album covers (first 3 albums)
        holder.albumCoversContainer.removeAllViews()
        list.albums.take(3).forEach { album ->
            val albumCover = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.album_cover_frame_small, holder.albumCoversContainer, false)
            val imageView = albumCover.findViewById<ImageView>(R.id.album_cover)

            if (album.coverImage.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(album.coverImage)
                    .placeholder(R.drawable.album_placeholder)
                    .error(R.drawable.album_placeholder)
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.album_placeholder)
            }

            holder.albumCoversContainer.addView(albumCover)
        }

        holder.itemView.setOnClickListener {
            onListClick(list)
        }
    }

    override fun getItemCount(): Int = lists.size
}