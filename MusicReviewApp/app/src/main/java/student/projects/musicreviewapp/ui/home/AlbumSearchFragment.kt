package student.projects.musicreviewapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.adapters.MusicAdapter
import student.projects.musicreviewapp.auth.FavoriteAlbumsManager
import student.projects.musicreviewapp.models.Music
import student.projects.musicreviewapp.network.SpotifyApiService

class AlbumSearchFragment : Fragment() {

    private lateinit var spotifyApiService: SpotifyApiService
    private lateinit var favoriteAlbumsManager: FavoriteAlbumsManager
    private lateinit var searchResultsAdapter: MusicAdapter
    private var selectedPosition: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_album_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spotifyApiService = SpotifyApiService(requireContext())
        favoriteAlbumsManager = FavoriteAlbumsManager(requireContext())
        selectedPosition = arguments?.getInt("position", -1) ?: -1

        setupViews(view)
    }

    private fun setupViews(view: View) {
        // Back button
        view.findViewById<ImageView>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }

        // Search input
        val searchInput = view.findViewById<EditText>(R.id.search_input)
        val searchButton = view.findViewById<ImageView>(R.id.search_button)

        // RecyclerView for search results
        val searchResultsRecycler = view.findViewById<RecyclerView>(R.id.search_results_recycler)
        searchResultsAdapter = MusicAdapter(emptyList())

        searchResultsAdapter.setOnItemClickListener { music ->
            // Add this album to favorites at the selected position
            addAlbumToFavorites(music)
        }

        searchResultsRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchResultsAdapter
        }

        // Search functionality
        searchButton.setOnClickListener {
            val query = searchInput.text.toString().trim()
            if (query.isNotEmpty()) {
                performSearch(query)
            }
        }

        // Also search when user presses enter
        searchInput.setOnEditorActionListener { _, _, _ ->
            val query = searchInput.text.toString().trim()
            if (query.isNotEmpty()) {
                performSearch(query)
            }
            true
        }
    }

    private fun performSearch(query: String) {
        spotifyApiService.searchMusic(query, object : SpotifyApiService.SpotifyCallback<List<Music>> {
            override fun onSuccess(result: List<Music>) {
                searchResultsAdapter.updateData(result)
            }
            override fun onError(error: String) {
                android.widget.Toast.makeText(requireContext(), "Search failed: $error", android.widget.Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addAlbumToFavorites(music: Music) {
        try {
            val currentFavorites = favoriteAlbumsManager.getFavoriteAlbums().toMutableList()

            // If we're replacing an existing position
            if (selectedPosition != -1 && selectedPosition < currentFavorites.size) {
                currentFavorites[selectedPosition] = music
            } else {
                // Adding to the end
                currentFavorites.add(music)
            }

            favoriteAlbumsManager.updateFavoriteAlbums(currentFavorites)
            android.widget.Toast.makeText(requireContext(), "Album added to favorites!", android.widget.Toast.LENGTH_SHORT).show()

            // Navigate back to FavoriteAlbumsFragment
            findNavController().popBackStack(R.id.favoriteAlbumsFragment, false)
        } catch (e: Exception) {
            android.widget.Toast.makeText(requireContext(), "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}