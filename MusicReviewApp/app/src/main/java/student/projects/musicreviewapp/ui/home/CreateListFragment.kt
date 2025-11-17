package student.projects.musicreviewapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.auth.ListManager
import student.projects.musicreviewapp.models.UserList

class CreateListFragment : Fragment() {

    private lateinit var listManager: ListManager
    private var createdListId: String? = null // Store the created list ID

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        listManager = ListManager(requireContext())
        return inflater.inflate(R.layout.fragment_create_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
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

        // Save button
        view.findViewById<TextView>(R.id.save_button).setOnClickListener {
            createNewList()
        }

        // Add entries click
        view.findViewById<View>(R.id.add_entries_option).setOnClickListener {
            navigateToAddEntries()
        }
    }

    private fun createNewList() {
        val listName = requireView().findViewById<EditText>(R.id.list_name_input).text.toString()
        val description = requireView().findViewById<EditText>(R.id.description_input).text.toString()
        val tags = requireView().findViewById<EditText>(R.id.tags_input).text.toString()

        if (listName.isBlank()) {
            android.widget.Toast.makeText(requireContext(), "Please enter a list name", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val tagList = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        val newList = UserList(
            id = listManager.generateListId(),
            name = listName,
            description = description,
            tags = tagList,
            createdAt = listManager.getCurrentTimestamp()
        )

        listManager.createList(newList)
        createdListId = newList.id // Store the created list ID

        android.widget.Toast.makeText(requireContext(), "List created successfully", android.widget.Toast.LENGTH_SHORT).show()

        // Option 1: Automatically navigate to add entries after creating list
        navigateToAddEntries()

        // Option 2: Or if you want to let user choose, show a success message and let them click "Add entries"
        // The "Add entries" option in the layout will already be clickable
    }

    private fun navigateToAddEntries() {
        // Check if we have a created list ID
        if (createdListId == null) {
            android.widget.Toast.makeText(requireContext(), "Please create the list first", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        // Navigate to add entries with the list ID
        val bundle = Bundle().apply {
            putString("listId", createdListId)
        }
        findNavController().navigate(R.id.addEntriesFragment, bundle)
    }
}