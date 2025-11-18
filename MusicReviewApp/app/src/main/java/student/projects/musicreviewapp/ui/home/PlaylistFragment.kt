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
import student.projects.musicreviewapp.auth.FirebaseDataManager
import student.projects.musicreviewapp.models.Music

class PlaylistFragment : Fragment() {

    private lateinit var playlistAdapter: PlaylistAdapter
    private lateinit var dataManager: FirebaseDataManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataManager = FirebaseDataManager(requireContext())
        setupViews(view)
        loadPlaylistAlbums()
    }

    private fun setupViews(view: View) {
        // Setup back button
        view.findViewById<ImageView>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }

        // Setup RecyclerView with grid layout (4 columns)
        val playlistRecycler = view.findViewById<RecyclerView>(R.id.playlist_recycler)
        playlistAdapter = PlaylistAdapter()

        // Set click listener for albums
        playlistAdapter.onAlbumClick = { music ->
            navigateToAlbumDetail(music)
        }

        playlistRecycler.apply {
            layoutManager = GridLayoutManager(requireContext(), 4) // 4 columns grid
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
        dataManager.getPlaylist { playlistAlbums ->
            activity?.runOnUiThread {
                playlistAdapter.submitList(playlistAlbums)

                // Update album count
                view?.findViewById<TextView>(R.id.album_count)?.text = "${playlistAlbums.size} albums"

                // Show empty state if no albums
                if (playlistAlbums.isEmpty()) {
                    view?.findViewById<TextView>(R.id.empty_state_text)?.visibility = View.VISIBLE
                    view?.findViewById<RecyclerView>(R.id.playlist_recycler)?.visibility = View.GONE
                } else {
                    view?.findViewById<TextView>(R.id.empty_state_text)?.visibility = View.GONE
                    view?.findViewById<RecyclerView>(R.id.playlist_recycler)?.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh playlist when returning to this fragment
        loadPlaylistAlbums()
    }

    class PlaylistAdapter : RecyclerView.Adapter<PlaylistAdapter.AlbumViewHolder>() {
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
            val music = albums[position]

            // Load album cover from Spotify URL
            if (!music.coverImage.isNullOrEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(music.coverImage)
                    .placeholder(R.drawable.album_placeholder)
                    .error(R.drawable.album_placeholder)
                    .centerCrop()
                    .into(holder.albumCover)
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