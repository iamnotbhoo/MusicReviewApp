package student.projects.musicreviewapp.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import student.projects.musicreviewapp.auth.FirebaseDataManager
import student.projects.musicreviewapp.auth.FirebaseFavoriteAlbumsManager
import student.projects.musicreviewapp.models.Music

class FavoriteAlbumsFragment : Fragment() {

    private lateinit var dataManager: FirebaseDataManager
    private lateinit var favoritesAdapter: FavoriteAlbumsSettingsAdapter
    private var selectedPosition: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite_albums, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataManager = FirebaseDataManager(requireContext()) // Initialize here
        selectedPosition = arguments?.getInt("position", -1) ?: -1

        setupViews(view)
        loadFavorites()
    }

    override fun onResume() {
        super.onResume()
        Log.d("FavoriteAlbums", "Fragment resumed - reloading favorites")
        loadFavorites()
    }


    private fun setupViews(view: View) {
        // Setup back button
        view.findViewById<ImageView>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }

        // Setup title
        view.findViewById<TextView>(R.id.title_text).text = "Favorite Albums"

        // Setup RecyclerView with 2x2 grid layout (4 albums total)
        val favoritesRecycler = view.findViewById<RecyclerView>(R.id.favorites_recycler)
        favoritesAdapter = FavoriteAlbumsSettingsAdapter()

        favoritesAdapter.onAlbumClick = { album, position ->
            // This is an existing album - tap to replace
            navigateToAlbumSearch(position)
        }

        favoritesAdapter.onAddAlbumClick = { position ->
            navigateToAlbumSearch(position)
        }

        favoritesRecycler.apply {
            layoutManager = GridLayoutManager(requireContext(), 2) // 2 columns for 2x2 grid
            adapter = favoritesAdapter
        }
    }

    private fun navigateToAlbumSearch(position: Int) {
        val bundle = Bundle().apply {
            putInt("position", position)
        }
        findNavController().navigate(R.id.action_favoriteAlbumsFragment_to_albumSearchFragment, bundle)
    }

    private fun loadFavorites() {
        Log.d("FavoriteAlbums", "Loading favorites from data manager...")

        // Add a small delay to handle Firebase propagation timing
        Handler(Looper.getMainLooper()).postDelayed({
            dataManager.getFavoriteAlbums { favorites ->
                activity?.runOnUiThread {
                    Log.d("FavoriteAlbums", "Received ${favorites.size} favorites from data manager")

                    val currentFavorites = favorites.toMutableList()

                    // Fill remaining slots with placeholders
                    val emptySlots = 4 - currentFavorites.size
                    Log.d("FavoriteAlbums", "Empty slots to fill: $emptySlots")

                    for (i in 0 until emptySlots) {
                        currentFavorites.add(
                            Music(
                                id = "placeholder_${currentFavorites.size + i}",
                                title = "",
                                artist = "",
                                album = "",
                                releaseYear = 0,
                                genre = "",
                                coverImage = "",
                                averageRating = 0.0,
                                reviewCount = 0
                            )
                        )
                    }

                    Log.d("FavoriteAlbums", "Final list size for adapter: ${currentFavorites.size}")
                    favoritesAdapter.submitList(currentFavorites)

                    // Check if our added album is in the list
                    val hasUTOPIA = currentFavorites.any { it.title == "UTOPIA" }
                    Log.d("FavoriteAlbums", "UTOPIA album in list: $hasUTOPIA")

                    // If still not showing, try one more time after a delay
                    if (!hasUTOPIA && favorites.isEmpty()) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            loadFavorites() // Retry once
                        }, 1000)
                    }
                }
            }
        }, 1000) // 1 second delay
    }


    // This method should be called from AlbumSearchFragment when an album is selected
    private fun addAlbumToFavorites(music: Music) {
        Log.d("FavoriteAlbums", "Starting addAlbumToFavorites for: ${music.title}")

        dataManager.getFavoriteAlbums { currentFavorites ->
            Log.d("FavoriteAlbums", "Current favorites count: ${currentFavorites.size}")

            val mutableFavorites = currentFavorites.toMutableList()

            // Check if we're replacing or adding
            if (selectedPosition != -1 && selectedPosition < mutableFavorites.size) {
                Log.d("FavoriteAlbums", "Replacing album at position $selectedPosition")
                mutableFavorites[selectedPosition] = music
            } else if (mutableFavorites.size < 4) {
                Log.d("FavoriteAlbums", "Adding new album at position ${mutableFavorites.size}")
                mutableFavorites.add(music)
            } else {
                Log.d("FavoriteAlbums", "Replacing last album (at capacity)")
                mutableFavorites[3] = music
            }

            Log.d("FavoriteAlbums", "Updated favorites count: ${mutableFavorites.size}")

            // Update with callback
            dataManager.updateFavoriteAlbums(mutableFavorites) { success ->
                activity?.runOnUiThread {
                    if (success) {
                        Log.d("FavoriteAlbums", "Successfully updated favorites")
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Album added to favorites!",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        // Reload the favorites to reflect changes
                        loadFavorites()
                        findNavController().popBackStack()
                    } else {
                        Log.e("FavoriteAlbums", "Failed to update favorites")
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Error adding album to favorites",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    class FavoriteAlbumsSettingsAdapter : RecyclerView.Adapter<FavoriteAlbumsSettingsAdapter.FavoriteAlbumViewHolder>() {
        private var albums = listOf<Music>()
        var onAlbumClick: ((Music, Int) -> Unit)? = null
        var onAddAlbumClick: ((Int) -> Unit)? = null

        class FavoriteAlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val albumCover: ImageView = itemView.findViewById(R.id.album_cover)
            val addButton: ImageView = itemView.findViewById(R.id.add_button)
            val albumTitle: TextView = itemView.findViewById(R.id.album_title)
            val artistName: TextView = itemView.findViewById(R.id.artist_name)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteAlbumViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_favorite_album_settings, parent, false)
            return FavoriteAlbumViewHolder(view)
        }

        override fun onBindViewHolder(holder: FavoriteAlbumViewHolder, position: Int) {
            val album = albums[position]

            // Check if this is a placeholder (empty slot)
            val isPlaceholder = album.id.startsWith("placeholder")

            if (isPlaceholder) {
                // Show add button for empty slots
                holder.addButton.visibility = View.VISIBLE
                holder.albumTitle.visibility = View.GONE
                holder.artistName.visibility = View.GONE
                holder.albumCover.setImageResource(R.drawable.album_placeholder)
                holder.albumCover.alpha = 0.5f

                holder.addButton.setOnClickListener {
                    onAddAlbumClick?.invoke(position)
                }

                holder.albumCover.setOnClickListener {
                    onAddAlbumClick?.invoke(position)
                }
            } else {
                // Show actual album (no remove button)
                holder.addButton.visibility = View.GONE
                holder.albumTitle.visibility = View.VISIBLE
                holder.artistName.visibility = View.VISIBLE
                holder.albumCover.alpha = 1.0f

                // Set album info
                holder.albumTitle.text = album.title
                holder.artistName.text = album.artist

                // Load album cover from Spotify
                if (album.coverImage.isNotEmpty()) {
                    Glide.with(holder.itemView.context)
                        .load(album.coverImage)
                        .placeholder(R.drawable.album_placeholder)
                        .error(R.drawable.album_placeholder)
                        .into(holder.albumCover)
                } else {
                    holder.albumCover.setImageResource(R.drawable.album_placeholder)
                }

                // Tap existing album to replace it
                holder.albumCover.setOnClickListener {
                    onAlbumClick?.invoke(album, position)
                }
            }
        }

        override fun getItemCount(): Int = albums.size

        fun submitList(newAlbums: List<Music>) {
            albums = newAlbums
            notifyDataSetChanged()
        }
    }
}