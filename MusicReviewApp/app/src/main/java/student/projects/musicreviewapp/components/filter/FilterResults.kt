package student.projects.musicreviewapp.components.filter

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.adapters.MusicAdapter
import student.projects.musicreviewapp.models.Music

class FilterResults @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var resultsRecyclerView: RecyclerView
    private lateinit var noResultsText: TextView
    private lateinit var resultsTitle: TextView

    private val musicAdapter = MusicAdapter(emptyList())

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_filter_results, this, true)
        setupViews()
    }

    private fun setupViews() {
        resultsRecyclerView = findViewById(R.id.results_recycler_view)
        noResultsText = findViewById(R.id.no_results_text)
        resultsTitle = findViewById(R.id.results_title)

        resultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = musicAdapter
        }
    }

    fun setResults(musicList: List<Music>) {
        if (musicList.isEmpty()) {
            noResultsText.visibility = View.VISIBLE
            resultsRecyclerView.visibility = View.GONE
        } else {
            noResultsText.visibility = View.GONE
            resultsRecyclerView.visibility = View.VISIBLE
            musicAdapter.updateData(musicList)
        }

        resultsTitle.text = "Music results (${musicList.size})"
    }
}