package student.projects.musicreviewapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.auth.ListManager
import student.projects.musicreviewapp.models.UserList
import student.projects.musicreviewapp.models.Music

class CreateListFragment : Fragment() {

    private lateinit var listManager: ListManager
    private var tempList: UserList? = null
    private val selectedAlbums = mutableListOf<Music>()
    private var albumToAdd: Music? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        albumToAdd = arguments?.getParcelable("albumToAdd")
    }

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
        setupFragmentResultListener()

        // If albumToAdd exists, add it to selected albums
        albumToAdd?.let { album ->
            if (!selectedAlbums.contains(album)) {
                selectedAlbums.add(album)
                updateSelectedAlbumsCount()
                Log.d("CreateListFragment", "Added album from bundle: ${album.title}")
            }
        }
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
            saveList()
        }

        // Add entries click
        view.findViewById<View>(R.id.add_entries_option).setOnClickListener {
            navigateToAddEntries()
        }

        // Update UI to show selected albums count
        updateSelectedAlbumsCount()
    }

    private fun setupFragmentResultListener() {
        // Listen for results from AddEntriesFragment
        parentFragmentManager.setFragmentResultListener("addEntriesRequest", this) { requestKey, bundle ->
            val selectedAlbumsList = bundle.getParcelableArrayList<Music>("selectedAlbums")
            selectedAlbumsList?.let { albums ->
                selectedAlbums.clear()
                selectedAlbums.addAll(albums)
                updateSelectedAlbumsCount()

                // Debug log
                Log.d("CreateListFragment", "Received ${albums.size} albums from AddEntries")
                Toast.makeText(requireContext(), "Added ${albums.size} albums to list", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToAddEntries() {
        // Create a temporary list with current data to pass to AddEntriesFragment
        val listName = requireView().findViewById<EditText>(R.id.list_name_input).text.toString()
        val description = requireView().findViewById<EditText>(R.id.description_input).text.toString()
        val tags = requireView().findViewById<EditText>(R.id.tags_input).text.toString()

        val tagList = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        tempList = UserList(
            id = listManager.generateListId(),
            name = listName,
            description = description,
            albums = selectedAlbums, // Pass current selected albums
            tags = tagList,
            createdAt = listManager.getCurrentTimestamp(),
            creator = "current_user"
        )

        val bundle = Bundle().apply {
            putParcelable("tempList", tempList)
        }
        findNavController().navigate(R.id.action_createListFragment_to_addEntriesFragment, bundle)
    }

    private fun saveList() {
        val listName = requireView().findViewById<EditText>(R.id.list_name_input).text.toString()

        if (listName.isBlank()) {
            Toast.makeText(requireContext(), "Please enter a list name", Toast.LENGTH_SHORT).show()
            return
        }

        val description = requireView().findViewById<EditText>(R.id.description_input).text.toString()
        val tags = requireView().findViewById<EditText>(R.id.tags_input).text.toString()
        val tagList = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        // Start with selected albums or empty list
        val initialAlbums = if (selectedAlbums.isNotEmpty()) {
            selectedAlbums.toMutableList()
        } else if (albumToAdd != null) {
            mutableListOf(albumToAdd!!)
        } else {
            mutableListOf()
        }

        if (initialAlbums.isEmpty()) {
            Toast.makeText(requireContext(), "Please add at least one album to your list", Toast.LENGTH_SHORT).show()
            return
        }

        val finalList = UserList(
            id = tempList?.id ?: listManager.generateListId(),
            name = listName,
            description = description,
            albums = initialAlbums,
            tags = tagList,
            createdAt = listManager.getCurrentTimestamp(),
            creator = "current_user"
        )

        listManager.createList(finalList)

        if (albumToAdd != null) {
            Toast.makeText(requireContext(), "List created with ${albumToAdd!!.title}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "List saved successfully!", Toast.LENGTH_SHORT).show()
        }

        // Navigate back to lists page
        findNavController().popBackStack(R.id.userListsFragment, false)
    }

    private fun updateSelectedAlbumsCount() {
        val addEntriesOption = requireView().findViewById<View>(R.id.add_entries_option)
        val textView = addEntriesOption.findViewById<TextView>(android.R.id.text1) ?: return

        if (selectedAlbums.isNotEmpty()) {
            textView.text = "Albums (${selectedAlbums.size} selected)"
        } else {
            textView.text = "Add entries..."
        }
    }

    // This will be called when we return from AddEntriesFragment
    override fun onResume() {
        super.onResume()
        // Update the selected albums count when returning from AddEntries
        updateSelectedAlbumsCount()
    }

    // Call this method from AddEntriesFragment to update the selected albums
    fun updateSelectedAlbums(albums: List<Music>) {
        selectedAlbums.clear()
        selectedAlbums.addAll(albums)
        updateSelectedAlbumsCount()
    }
}