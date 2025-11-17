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
import student.projects.musicreviewapp.models.UserList
import student.projects.musicreviewapp.network.SpotifyApiService
import android.widget.ProgressBar
import android.widget.Toast

class AddEntriesFragment : Fragment() {

    private lateinit var searchAdapter: SearchAdapter
    private lateinit var spotifyApiService: SpotifyApiService
    private lateinit var searchInput: EditText
    private lateinit var searchRecycler: RecyclerView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var recentSearchesText: TextView
    private lateinit var doneButton: TextView

    private val selectedAlbums = mutableListOf<Music>()
    private var tempList: UserList? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        spotifyApiService = SpotifyApiService(requireContext())

        // Get temp list from arguments
        arguments?.let { bundle ->
            tempList = bundle.getParcelable("tempList")
            tempList?.albums?.let { albums ->
                selectedAlbums.clear()
                selectedAlbums.addAll(albums)
            }
        }

        return inflater.inflate(R.layout.fragment_add_entries, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setupSearch()
        updateDoneButton()
    }

    private fun setupViews(view: View) {
        // Back button
        view.findViewById<ImageView>(R.id.back_button).setOnClickListener {
            goBackToCreateList()
        }


        // Done button
        doneButton = view.findViewById<TextView>(R.id.done_button)
        doneButton.setOnClickListener {
            goBackToCreateList()
        }

        // Initialize views
        searchInput = view.findViewById(R.id.search_input)
        searchRecycler = view.findViewById(R.id.search_recycler)
        loadingIndicator = view.findViewById(R.id.loading_indicator)
        recentSearchesText = view.findViewById(R.id.recent_searches_text)

        // Setup RecyclerView for search results
        searchRecycler.layoutManager = LinearLayoutManager(requireContext())
        searchAdapter = SearchAdapter(selectedAlbums) { album ->
            toggleAlbumSelection(album)
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
                if (query.length >= 2) {
                    performSearch(query)
                } else if (query.isEmpty()) {
                    showRecentSearches()
                }
            }
        })

        showRecentSearches()
    }

    private fun showRecentSearches() {
        val recentSearches = emptyList<Music>()
        searchAdapter.submitList(recentSearches)
        recentSearchesText.visibility = View.VISIBLE
        searchRecycler.visibility = View.GONE
    }

    private fun performSearch(query: String) {
        showLoading()
        recentSearchesText.visibility = View.GONE
        searchRecycler.visibility = View.VISIBLE

        spotifyApiService.searchMusic(
            query,
            object : SpotifyApiService.SpotifyCallback<List<Music>> {
                override fun onSuccess(result: List<Music>) {
                    hideLoading()
                    searchAdapter.submitList(result)
                    Log.d("SpotifySearch", "Search completed: ${result.size} results")
                }

                override fun onError(error: String) {
                    hideLoading()
                    Toast.makeText(requireContext(), "Search failed: $error", Toast.LENGTH_SHORT)
                        .show()
                    searchAdapter.submitList(emptyList())
                }
            })
    }

    private fun toggleAlbumSelection(album: Music) {
        val existingAlbum = selectedAlbums.find { it.id == album.id }
        if (existingAlbum != null) {
            selectedAlbums.remove(existingAlbum)
            Toast.makeText(requireContext(), "Removed ${album.title}", Toast.LENGTH_SHORT).show()
        } else {
            selectedAlbums.add(album)
            Toast.makeText(requireContext(), "Added ${album.title}", Toast.LENGTH_SHORT).show()
        }
        searchAdapter.updateSelectedAlbums(selectedAlbums)
        updateDoneButton()
    }

    private fun updateDoneButton() {
        if (selectedAlbums.isNotEmpty()) {
            doneButton.text = "Done (${selectedAlbums.size})"
            doneButton.isEnabled = true
            doneButton.alpha = 1.0f
        } else {
            doneButton.text = "Done"
            doneButton.isEnabled = true
            doneButton.alpha = 1.0f
        }
    }

    private fun goBackToCreateList() {
        // Pass the selected albums back to CreateListFragment using Fragment Result API
        val resultBundle = Bundle().apply {
            putParcelableArrayList("selectedAlbums", ArrayList(selectedAlbums))
        }

        // Use setFragmentResult to pass data back
        parentFragmentManager.setFragmentResult("addEntriesRequest", resultBundle)

        // Navigate back
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

    // REMOVED THE NESTED AddEntriesFragment CLASS - THIS WAS THE PROBLEM

    class SearchAdapter(
        private var selectedAlbums: List<Music>,
        private val onAlbumClick: (Music) -> Unit
    ) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

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
                .inflate(R.layout.item_search_result_add, parent, false)
            return SearchViewHolder(view)
        }

        override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
            val album = albums[position]
            val isSelected = selectedAlbums.any { it.id == album.id }

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

            // Update selection UI - ONLY use the add button
            if (isSelected) {
                holder.addButton.setImageResource(R.drawable.ic_check)
                holder.addButton.setColorFilter(holder.itemView.context.getColor(R.color.purple))
                holder.itemView.setBackgroundColor(holder.itemView.context.getColor(R.color.sign_in_button))
            } else {
                holder.addButton.setImageResource(R.drawable.ic_add)
                holder.addButton.setColorFilter(holder.itemView.context.getColor(android.R.color.darker_gray))
                holder.itemView.setBackgroundColor(holder.itemView.context.getColor(android.R.color.transparent))
            }

            // Add button click
            holder.addButton.setOnClickListener {
                onAlbumClick(album)
            }

            // Whole item click
            holder.itemView.setOnClickListener {
                onAlbumClick(album)
            }
        }

        override fun getItemCount(): Int = albums.size

        fun submitList(newAlbums: List<Music>) {
            albums = newAlbums
            notifyDataSetChanged()
        }

        fun updateSelectedAlbums(newSelectedAlbums: List<Music>) {
            selectedAlbums = newSelectedAlbums
            notifyDataSetChanged()
        }
    }
}