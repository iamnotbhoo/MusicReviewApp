package student.projects.musicreviewapp.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.models.Music
import student.projects.musicreviewapp.network.SpotifyApiService
import student.projects.musicreviewapp.models.AlbumDetails
import android.widget.ProgressBar
import android.widget.Toast

class AddEntriesFragment : Fragment() {

    private lateinit var searchAdapter: SearchAdapter
    private lateinit var spotifyApiService: SpotifyApiService
    private lateinit var searchInput: EditText
    private lateinit var searchRecycler: RecyclerView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var recentSearchesText: TextView

    // Store the list ID if you need to know which list we're adding to
    private var currentListId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        spotifyApiService = SpotifyApiService(requireContext())

        // Get list ID from arguments if passed
        arguments?.let { bundle ->
            currentListId = bundle.getString("listId")
        }

        return inflater.inflate(R.layout.fragment_add_entries, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupSearch()
    }

    private fun setupViews(view: View) {
        // Back button
        view.findViewById<ImageView>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }

        // Cancel button
        view.findViewById<TextView>(R.id.cancel_button).setOnClickListener {
            findNavController().popBackStack()
        }

        // Initialize views
        searchInput = view.findViewById(R.id.search_input)
        searchRecycler = view.findViewById(R.id.search_recycler)
        loadingIndicator = view.findViewById(R.id.loading_indicator)
        recentSearchesText = view.findViewById(R.id.recent_searches_text)

        // Setup RecyclerView for search results
        searchRecycler.layoutManager = LinearLayoutManager(requireContext())
        searchAdapter = SearchAdapter { album ->
            // Add album to list and go back
            addAlbumToList(album)
        }
        searchRecycler.adapter = searchAdapter

        // Hide loading initially
        loadingIndicator.visibility = View.GONE
    }

    private fun setupSearch() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.length >= 2) { // Start searching after 2 characters
                    performSearch(query)
                } else if (query.isEmpty()) {
                    showRecentSearches()
                }
            }
        })

        // Show recent searches initially
        showRecentSearches()
    }

    private fun showRecentSearches() {
        // For now, show empty recent searches
        // You can implement recent searches storage
        val recentSearches = emptyList<Music>()
        searchAdapter.submitList(recentSearches)
        recentSearchesText.visibility = View.VISIBLE
        searchRecycler.visibility = View.GONE
    }

    private fun performSearch(query: String) {
        showLoading()
        recentSearchesText.visibility = View.GONE
        searchRecycler.visibility = View.VISIBLE

        // Use the existing searchMusic method instead of searchAlbums
        spotifyApiService.searchMusic(query, object : SpotifyApiService.SpotifyCallback<List<Music>> {
            override fun onSuccess(result: List<Music>) {
                hideLoading()
                searchAdapter.submitList(result)
                Log.d("SpotifySearch", "Search completed: ${result.size} results")
            }

            override fun onError(error: String) {
                hideLoading()
                Toast.makeText(requireContext(), "Search failed: $error", Toast.LENGTH_SHORT).show()
                searchAdapter.submitList(emptyList())
            }
        })
    }

    private fun addAlbumToList(album: Music) {
        // Here you would add the album to the current list
        // You'll need to pass the list ID and handle this in your ListManager

        Toast.makeText(requireContext(), "Added ${album.title} to list", Toast.LENGTH_SHORT).show()

        // Navigate back to list detail or stay for more additions
        findNavController().popBackStack()
    }

    private fun showLoading() {
        loadingIndicator.visibility = View.VISIBLE
        searchRecycler.visibility = View.GONE
    }

    private fun hideLoading() {
        loadingIndicator.visibility = View.GONE
        searchRecycler.visibility = View.VISIBLE
    }

    class SearchAdapter(private val onAlbumClick: (Music) -> Unit) :
        RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

        private var albums = listOf<Music>()

        class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val albumCover: ImageView = itemView.findViewById(R.id.album_cover)
            val albumTitle: TextView = itemView.findViewById(R.id.album_title)
            val albumArtist: TextView = itemView.findViewById(R.id.album_artist)
            val albumYear: TextView = itemView.findViewById(R.id.album_year)
            val addButton: ImageView = itemView.findViewById(R.id.add_button)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_search_result, parent, false)
            return SearchViewHolder(view)
        }

        override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
            val album = albums[position]

            holder.albumTitle.text = album.title
            holder.albumArtist.text = album.artist
            holder.albumYear.text = album.releaseYear.toString()

            // Load album cover
            if (album.coverImage.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(album.coverImage)
                    .placeholder(R.drawable.album_placeholder)
                    .error(R.drawable.album_placeholder)
                    .centerCrop()
                    .into(holder.albumCover)
            } else {
                holder.albumCover.setImageResource(R.drawable.album_placeholder)
            }

            // Add button click
            holder.addButton.setOnClickListener {
                onAlbumClick(album)
            }

            // Whole item click (optional)
            holder.itemView.setOnClickListener {
                onAlbumClick(album)
            }
        }

        override fun getItemCount(): Int = albums.size

        fun submitList(newAlbums: List<Music>) {
            albums = newAlbums
            notifyDataSetChanged()
        }
    }
}