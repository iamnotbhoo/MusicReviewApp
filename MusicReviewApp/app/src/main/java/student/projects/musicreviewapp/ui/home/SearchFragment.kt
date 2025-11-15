package student.projects.musicreviewapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.components.searching.SearchInputMobile

class SearchFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_wrapper, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchInput = view.findViewById<SearchInputMobile>(R.id.search_input_component)

        searchInput.onSearch = { query ->
            // Handle search - you can integrate this with your existing search logic
            performSearch(query)
        }
    }

    private fun performSearch(query: String) {
        // Show search results - you can use your existing FilterResults component
        android.widget.Toast.makeText(requireContext(), "Searching for: $query", android.widget.Toast.LENGTH_SHORT).show()

        // TODO: Integrate with your existing FilterResults component
        // You can show results in a RecyclerView below the search input
    }
}