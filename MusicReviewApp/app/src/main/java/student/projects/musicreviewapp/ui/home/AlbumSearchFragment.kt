package student.projects.musicreviewapp.ui.home

import android.os.Bundle
import android.util.Log
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
import student.projects.musicreviewapp.auth.FirebaseDataManager
import student.projects.musicreviewapp.models.Music
import student.projects.musicreviewapp.network.SpotifyApiService

class AlbumSearchFragment : Fragment() {

    private lateinit var spotifyApiService: SpotifyApiService

    private lateinit var searchResultsAdapter: MusicAdapter

    private lateinit var dataManager: FirebaseDataManager
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
        dataManager = FirebaseDataManager(requireContext())
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
        spotifyApiService.searchMusic(
            query,
            object : SpotifyApiService.SpotifyCallback<List<Music>> {
                override fun onSuccess(result: List<Music>) {
                    searchResultsAdapter.updateData(result)
                }

                override fun onError(error: String) {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Search failed: $error",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun addAlbumToFavorites(music: Music) {
        try {
            Log.d("AlbumSearch", "Adding album to favorites: ${music.title} by ${music.artist}")

            // Use dataManager consistently
            dataManager.getFavoriteAlbums { currentFavorites ->
                val mutableFavorites = currentFavorites.toMutableList()

                Log.d("AlbumSearch", "Current favorites count: ${currentFavorites.size}")
                Log.d("AlbumSearch", "Selected position: $selectedPosition")

                // Your existing logic for adding/replacing...
                if (selectedPosition != -1 && selectedPosition < mutableFavorites.size) {
                    Log.d("AlbumSearch", "Replacing album at position $selectedPosition")
                    mutableFavorites[selectedPosition] = music
                } else {
                    if (mutableFavorites.size < 4) {
                        mutableFavorites.add(music)
                    } else {
                        mutableFavorites[3] = music
                    }
                }

                Log.d("AlbumSearch", "Updated favorites count: ${mutableFavorites.size}")

                // Use dataManager consistently here too
                dataManager.updateFavoriteAlbums(mutableFavorites) { success ->
                    activity?.runOnUiThread {
                        if (success) {
                            android.widget.Toast.makeText(
                                requireContext(),
                                "Album added to favorites!",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            findNavController().popBackStack()
                        } else {
                            android.widget.Toast.makeText(
                                requireContext(),
                                "Error adding album to favorites",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AlbumSearch", "Error adding to favorites: ${e.message}", e)
            android.widget.Toast.makeText(
                requireContext(),
                "Error: ${e.message}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
}