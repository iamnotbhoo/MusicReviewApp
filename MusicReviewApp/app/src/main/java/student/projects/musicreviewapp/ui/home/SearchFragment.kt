package student.projects.musicreviewapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var emptyStateText: TextView
    private lateinit var searchPromptText: TextView

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
        loadingIndicator = view.findViewById(R.id.loading_indicator)
        emptyStateText = view.findViewById(R.id.empty_state_text)
        searchPromptText = view.findViewById(R.id.search_prompt_text)

        // Initialize UI state
        showSearchPrompt()

        // Setup RecyclerView
        musicAdapter = MusicAdapter(emptyList())

        // Set click listener for music items
        musicAdapter.setOnItemClickListener { music ->
            navigateToAlbumDetail(music)
        }

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
            showSearchPrompt()
            musicAdapter.updateData(emptyList())
            return
        }

        showLoading()

        spotifyApiService.searchMusic(query, object : SpotifyApiService.SpotifyCallback<List<Music>> {
            override fun onSuccess(result: List<Music>) {
                hideLoading()
                musicAdapter.updateData(result)

                // Debug logging
                result.forEach { music ->
                    Log.d("SearchDebug", "Result: ${music.title} by ${music.artist} - Cover: ${music.coverImage}")
                }

                if (result.isEmpty()) {
                    showEmptyState("No results found for '$query'")
                } else {
                    showResults()
                }
            }

            override fun onError(error: String) {
                hideLoading()
                showEmptyState("Search failed: $error")
                musicAdapter.updateData(emptyList())
                Log.e("SearchFragment", "Search error: $error")
            }
        })
    }

    private fun showLoading() {
        loadingIndicator.isVisible = true
        searchResultsRecyclerView.isVisible = false
        emptyStateText.isVisible = false
        searchPromptText.isVisible = false
    }

    private fun hideLoading() {
        loadingIndicator.isVisible = false
    }

    private fun showResults() {
        searchResultsRecyclerView.isVisible = true
        emptyStateText.isVisible = false
        searchPromptText.isVisible = false
    }

    private fun showEmptyState(message: String) {
        emptyStateText.text = message
        emptyStateText.isVisible = true
        searchResultsRecyclerView.isVisible = false
        searchPromptText.isVisible = false
    }

    private fun showSearchPrompt() {
        searchPromptText.isVisible = true
        searchResultsRecyclerView.isVisible = false
        emptyStateText.isVisible = false
        loadingIndicator.isVisible = false
    }

    private fun navigateToAlbumDetail(music: Music) {
        val bundle = Bundle().apply {
            putParcelable("album", music)
        }
        findNavController().navigate(R.id.action_searchFragment_to_albumDetailFragment, bundle)
    }
}