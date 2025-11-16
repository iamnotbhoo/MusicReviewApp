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
import student.projects.musicreviewapp.auth.FavoriteAlbumsManager
import student.projects.musicreviewapp.models.Music

class FavoriteAlbumsFragment : Fragment() {

    private lateinit var favoriteAlbumsManager: FavoriteAlbumsManager
    private lateinit var favoritesAdapter: FavoriteAlbumsSettingsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite_albums, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        favoriteAlbumsManager = FavoriteAlbumsManager(requireContext())

        setupViews(view)
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
            if (album.id.startsWith("placeholder")) {
                // This is an empty slot - navigate to search
                navigateToAlbumSearch(position)
            } else {
                // This is an existing album - tap to replace
                navigateToAlbumSearch(position)
            }
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
        val currentFavorites = favoriteAlbumsManager.getFavoriteAlbums().toMutableList()

        // Fill remaining slots with placeholders
        val emptySlots = 4 - currentFavorites.size
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

        favoritesAdapter.submitList(currentFavorites)
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