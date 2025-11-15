package student.projects.musicreviewapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.adapters.MusicAdapter
import student.projects.musicreviewapp.models.Music

class PopularMusicFragment : Fragment() {

    private lateinit var popularMusicRecyclerView: RecyclerView
    private lateinit var musicAdapter: MusicAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_popular_music, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        popularMusicRecyclerView = view.findViewById(R.id.popular_music_recycler_view)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        musicAdapter = MusicAdapter(getPopularMusic())
        popularMusicRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 3) // 3 columns for grid
            adapter = musicAdapter
        }
    }

    private fun getPopularMusic(): List<Music> {
        return listOf(
            Music(
                id = "1", title = "Song 1", artist = "Artist 1",
                album = "Album 1", releaseYear = 2023, genre = "Pop",
                coverImage = "", averageRating = 4.5, reviewCount = 120
            ),
            Music(
                id = "2", title = "Song 2", artist = "Artist 2",
                album = "Album 2", releaseYear = 2023, genre = "Rock",
                coverImage = "", averageRating = 4.2, reviewCount = 89
            ),
            // Add more music items...
        )
    }
}