package student.projects.musicreviewapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.adapters.MusicAdapter
import student.projects.musicreviewapp.components.searching.SearchInputMobile
import student.projects.musicreviewapp.models.Music
import student.projects.musicreviewapp.network.SpotifyApiService

class SearchFragment : Fragment() {

    private lateinit var spotifyApiService: SpotifyApiService
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var musicAdapter: MusicAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        spotifyApiService = SpotifyApiService(requireContext())
        return inflater.inflate(R.layout.fragment_search_wrapper, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchInput = view.findViewById<SearchInputMobile>(R.id.search_input_component)
        searchResultsRecyclerView = view.findViewById(R.id.search_results_recycler_view)

        // Setup RecyclerView
        musicAdapter = MusicAdapter(emptyList())
        searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = musicAdapter
        }

        searchInput.onSearch = { query ->
            performSearch(query)
        }
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            showToast("Please enter a search term")
            return
        }

        spotifyApiService.searchTracks(query, object : SpotifyApiService.SpotifyCallback<List<Music>> {
            override fun onSuccess(result: List<Music>) {
                musicAdapter.updateData(result)
                if (result.isEmpty()) {
                    showToast("No results found for '$query'")
                } else {
                    showToast("Found ${result.size} results")
                }
            }
            override fun onError(error: String) {
                showToast("Search failed: $error")
            }
        })
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }
}