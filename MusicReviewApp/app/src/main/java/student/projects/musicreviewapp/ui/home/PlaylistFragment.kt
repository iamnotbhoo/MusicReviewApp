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
import student.projects.musicreviewapp.models.Music

class PlaylistFragment : Fragment() {

    private lateinit var playlistAdapter: PlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        loadPlaylistAlbums()
    }

    private fun setupViews(view: View) {
        // Setup back button
        view.findViewById<ImageView>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }

        // Setup RecyclerView with grid layout
        val playlistRecycler = view.findViewById<RecyclerView>(R.id.playlist_recycler)
        playlistAdapter = PlaylistAdapter()

        // Set click listener for albums
        playlistAdapter.onAlbumClick = { music ->
            navigateToAlbumDetail(music)
        }

        playlistRecycler.apply {
            layoutManager = GridLayoutManager(requireContext(), 3) // 3 columns grid
            adapter = playlistAdapter
        }
    }

    private fun navigateToAlbumDetail(music: Music) {
        val bundle = Bundle().apply {
            putParcelable("album", music)
        }
        findNavController().navigate(R.id.action_playlistFragment_to_albumDetailFragment, bundle)
    }

    private fun loadPlaylistAlbums() {
        val mockPlaylistAlbums = getMockPlaylistAlbums()
        playlistAdapter.submitList(mockPlaylistAlbums)

        // Update album count
        view?.findViewById<TextView>(R.id.album_count)?.text = "${mockPlaylistAlbums.size} albums"
    }

    private fun getMockPlaylistAlbums(): List<Music> {
        return listOf(
            Music(
                id = "1",
                title = "Rodeo",
                artist = "Travis Scott",
                album = "Rodeo",
                releaseYear = 2015,
                genre = "Hip-Hop",
                coverImage = "rodeo_travis",
                averageRating = 4.5,
                reviewCount = 120
            ),
            Music(
                id = "2",
                title = "DAMN",
                artist = "Kendrick Lamar",
                album = "DAMN",
                releaseYear = 2017,
                genre = "Hip-Hop",
                coverImage = "damn_kendrick",
                averageRating = 4.8,
                reviewCount = 200
            ),
            Music(
                id = "3",
                title = "A Day At The Races",
                artist = "Queen",
                album = "A Day At The Races",
                releaseYear = 1976,
                genre = "Rock",
                coverImage = "dayattheraces_queen",
                averageRating = 4.6,
                reviewCount = 150
            ),
            Music(
                id = "4",
                title = "Guard Dog",
                artist = "Searows",
                album = "Guard Dog",
                releaseYear = 2022,
                genre = "Indie Folk",
                coverImage = "guarddog_searows",
                averageRating = 4.2,
                reviewCount = 80
            ),
            Music(
                id = "5",
                title = "The Blueprint",
                artist = "Jay Z",
                album = "The Blueprint",
                releaseYear = 2001,
                genre = "Hip-Hop",
                coverImage = "blueprint_jay",
                averageRating = 4.7,
                reviewCount = 180
            ),
            Music(
                id = "6",
                title = "UTOPIA",
                artist = "Travis Scott",
                album = "UTOPIA",
                releaseYear = 2023,
                genre = "Hip-Hop",
                coverImage = "utopia_travis",
                averageRating = 4.4,
                reviewCount = 160
            ),
            Music(
                id = "7",
                title = "Blonde",
                artist = "Frank Ocean",
                album = "Blonde",
                releaseYear = 2016,
                genre = "R&B",
                coverImage = "",
                averageRating = 4.9,
                reviewCount = 220
            ),
            Music(
                id = "8",
                title = "After Hours",
                artist = "The Weeknd",
                album = "After Hours",
                releaseYear = 2020,
                genre = "R&B",
                coverImage = "",
                averageRating = 4.5,
                reviewCount = 190
            ),
            Music(
                id = "9",
                title = "Folklore",
                artist = "Taylor Swift",
                album = "Folklore",
                releaseYear = 2020,
                genre = "Pop",
                coverImage = "",
                averageRating = 4.6,
                reviewCount = 210
            )
        )
    }

    class PlaylistAdapter : RecyclerView.Adapter<PlaylistAdapter.AlbumViewHolder>() {
        private var albums = listOf<Music>()

        var onAlbumClick: ((Music) -> Unit)? = null

        class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val albumCover: ImageView = itemView.findViewById(R.id.album_cover)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_playlist_album, parent, false)
            return AlbumViewHolder(view)
        }

        override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
            val music = albums[position]

            // Load album cover
            if (!music.coverImage.isNullOrEmpty()) {
                try {
                    val resourceId = holder.itemView.context.resources.getIdentifier(
                        music.coverImage,
                        "drawable",
                        holder.itemView.context.packageName
                    )
                    if (resourceId != 0) {
                        Glide.with(holder.itemView.context)
                            .load(resourceId)
                            .placeholder(R.drawable.album_placeholder)
                            .error(R.drawable.album_placeholder)
                            .into(holder.albumCover)
                    } else {
                        holder.albumCover.setImageResource(R.drawable.album_placeholder)
                    }
                } catch (e: Exception) {
                    holder.albumCover.setImageResource(R.drawable.album_placeholder)
                }
            } else {
                holder.albumCover.setImageResource(R.drawable.album_placeholder)
            }

            holder.itemView.setOnClickListener {
                onAlbumClick?.invoke(music)
            }
        }

        override fun getItemCount(): Int = albums.size

        fun submitList(newAlbums: List<Music>) {
            albums = newAlbums
            notifyDataSetChanged()
        }
    }
}