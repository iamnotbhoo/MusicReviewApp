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
import student.projects.musicreviewapp.auth.AuthManager
import student.projects.musicreviewapp.auth.FirebaseDataManager
import student.projects.musicreviewapp.models.UserList
import student.projects.musicreviewapp.models.Music

class CreateListFragment : Fragment() {

    private lateinit var firebaseDataManager: FirebaseDataManager
    private lateinit var authManager: AuthManager
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
        authManager = AuthManager()
        firebaseDataManager = FirebaseDataManager(requireContext())
        return inflater.inflate(R.layout.fragment_create_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setupFragmentResultListener()

        // If albumToAdd exists, add it to selected albums
        albumToAdd?.let { album ->
            if (!selectedAlbums.any { it.id == album.id }) {
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

                Log.d("CreateListFragment", "Received ${albums.size} albums from AddEntries")
                Toast.makeText(requireContext(), "Added ${albums.size} albums to list", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToAddEntries() {
        val listName = requireView().findViewById<EditText>(R.id.list_name_input).text.toString()
        val description = requireView().findViewById<EditText>(R.id.description_input).text.toString()
        val tags = requireView().findViewById<EditText>(R.id.tags_input).text.toString()

        val tagList = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        val userId = getCurrentUserId()
        if (userId == null) {
            Toast.makeText(requireContext(), "Please log in to create lists", Toast.LENGTH_SHORT).show()
            return
        }

        tempList = UserList(
            id = generateListId(),
            name = listName,
            description = description,
            albums = selectedAlbums.toMutableList(),
            tags = tagList,
            createdAt = System.currentTimeMillis().toString(),
            creator = userId,
            isPublic = true,
            likes = 0, // Initialize with 0 likes
            liked = false // Initialize as not liked
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

        val userId = getCurrentUserId()
        if (userId == null) {
            Toast.makeText(requireContext(), "Please log in to create lists", Toast.LENGTH_SHORT).show()
            return
        }

        val description = requireView().findViewById<EditText>(R.id.description_input).text.toString()
        val tags = requireView().findViewById<EditText>(R.id.tags_input).text.toString()
        val tagList = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        // Start with selected albums or empty list
        val finalAlbums = if (selectedAlbums.isNotEmpty()) {
            selectedAlbums.toMutableList()
        } else if (albumToAdd != null) {
            mutableListOf(albumToAdd!!)
        } else {
            mutableListOf()
        }

        if (finalAlbums.isEmpty()) {
            Toast.makeText(requireContext(), "Please add at least one album to your list", Toast.LENGTH_SHORT).show()
            return
        }

        val finalList = UserList(
            id = generateListId(),
            name = listName,
            description = description,
            albums = finalAlbums,
            tags = tagList,
            createdAt = System.currentTimeMillis().toString(),
            creator = userId,
            isPublic = true,
            likes = 0, // Initialize with 0 likes
            liked = false // Initialize as not liked
        )

        Log.d("CreateListFragment", "🔄 Creating list: ${finalList.name} for user: $userId")

        // Save list using FirebaseDataManager
        firebaseDataManager.createList(finalList) { success ->
            activity?.runOnUiThread {
                if (success) {
                    Log.d("CreateListFragment", "✅ List created successfully")
                    val message = if (albumToAdd != null) {
                        "List created with ${albumToAdd!!.title}"
                    } else {
                        "List saved successfully!"
                    }
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                    // Navigate back to lists page
                    findNavController().popBackStack(R.id.userListsFragment, false)
                } else {
                    Log.e("CreateListFragment", "❌ Failed to save list")
                    Toast.makeText(requireContext(), "Failed to save list. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getCurrentUserId(): String? {
        return authManager.getCurrentUid()
    }

    private fun generateListId(): String {
        return "list_${System.currentTimeMillis()}"
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

    override fun onResume() {
        super.onResume()
        updateSelectedAlbumsCount()
    }

    fun updateSelectedAlbums(albums: List<Music>) {
        selectedAlbums.clear()
        selectedAlbums.addAll(albums)
        updateSelectedAlbumsCount()
    }
}