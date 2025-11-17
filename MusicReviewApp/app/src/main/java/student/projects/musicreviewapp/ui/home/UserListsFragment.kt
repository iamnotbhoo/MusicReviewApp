package student.projects.musicreviewapp.ui.home

import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.auth.ListManager
import student.projects.musicreviewapp.models.UserList

class UserListsFragment : Fragment() {

    private lateinit var listManager: ListManager
    private lateinit var listsAdapter: ListsAdapter
    private lateinit var searchInput: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        listManager = ListManager(requireContext())
        return inflater.inflate(R.layout.fragment_user_lists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        loadLists()
    }

    private fun setupViews(view: View) {
        // Back button
        view.findViewById<ImageView>(R.id.back_button)?.setOnClickListener {
            findNavController().popBackStack()
        }

        // Add new list button
        view.findViewById<ImageView>(R.id.add_list_button)?.setOnClickListener {
            navigateToCreateList()
        }

        // Search functionality
        searchInput = view.findViewById(R.id.search_input)
        setupSearch()

        // Setup RecyclerView
        val listsRecycler = view.findViewById<RecyclerView>(R.id.lists_recycler)
        listsRecycler.layoutManager = LinearLayoutManager(requireContext())
        listsAdapter = ListsAdapter(
            onListClick = { list ->
                navigateToListDetail(list)
            },
            onListDelete = { list ->
                showDeleteConfirmation(list)
            }
        )
        listsRecycler.adapter = listsAdapter
    }

    private fun setupSearch() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                filterLists(s.toString())
            }
        })
    }

    private fun filterLists(query: String) {
        val allLists = listManager.getLists()
        if (query.isBlank()) {
            listsAdapter.submitList(allLists)
        } else {
            val filteredLists = allLists.filter { list ->
                list.name.contains(query, ignoreCase = true) ||
                        list.description.contains(query, ignoreCase = true) ||
                        list.tags.any { it.contains(query, ignoreCase = true) }
            }
            listsAdapter.submitList(filteredLists)
        }
        updateEmptyState()
    }

    private fun loadLists() {
        val lists = listManager.getLists()
        listsAdapter.submitList(lists)
        updateEmptyState()
    }

    private fun updateEmptyState() {
        val emptyState = requireView().findViewById<View>(R.id.empty_state)
        val listsRecycler = requireView().findViewById<RecyclerView>(R.id.lists_recycler)
        val searchQuery = searchInput.text.toString()

        val shouldShowEmptyState = listsAdapter.itemCount == 0

        if (shouldShowEmptyState) {
            emptyState?.visibility = View.VISIBLE
            listsRecycler?.visibility = View.GONE

            // Update empty state text based on search
            val emptyStateText = requireView().findViewById<TextView>(R.id.empty_state_text)
            val emptyStateSubtext = requireView().findViewById<TextView>(R.id.empty_state_subtext)

            if (searchQuery.isNotBlank()) {
                emptyStateText?.text = "No lists found"
                emptyStateSubtext?.text = "Try adjusting your search terms"
            } else {
                emptyStateText?.text = "No lists yet"
                emptyStateSubtext?.text = "Create your first list to organize your favorite albums"
            }
        } else {
            emptyState?.visibility = View.GONE
            listsRecycler?.visibility = View.VISIBLE
        }
    }

    private fun navigateToCreateList() {
        findNavController().navigate(R.id.action_userListsFragment_to_createListFragment)
    }

    private fun navigateToListDetail(list: UserList) {
        val bundle = Bundle().apply {
            putParcelable("list", list)
        }
        findNavController().navigate(R.id.action_userListsFragment_to_listDetailFragment, bundle)
    }

    private fun showDeleteConfirmation(list: UserList) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete List")
            .setMessage("Are you sure you want to delete \"${list.name}\"? This action cannot be undone.")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteList(list)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteList(list: UserList) {
        listManager.deleteList(list.id)
        loadLists() // Refresh the list
        android.widget.Toast.makeText(requireContext(), "List deleted", android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        loadLists()
    }

    class ListsAdapter(
        private val onListClick: (UserList) -> Unit,
        private val onListDelete: (UserList) -> Unit
    ) : RecyclerView.Adapter<ListsAdapter.ListViewHolder>() {

        private var lists = listOf<UserList>()

        class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val listTitle: TextView = itemView.findViewById(R.id.list_title)
            val listCreator: TextView = itemView.findViewById(R.id.list_creator)
            val listDescription: TextView = itemView.findViewById(R.id.list_description)
            val likeCount: TextView = itemView.findViewById(R.id.like_count)
            val albumCoversContainer: LinearLayout = itemView.findViewById(R.id.album_covers_container)
            val deleteButton: ImageView = itemView.findViewById(R.id.delete_button)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_user_list, parent, false)
            return ListViewHolder(view)
        }

        override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
            val list = lists[position]

            holder.listTitle.text = list.name
            holder.listCreator.text = "by ${list.creator}"
            holder.listDescription.text = list.description
            holder.likeCount.text = "${list.likes} likes"

            // Load album covers
            holder.albumCoversContainer.removeAllViews()
            list.albums.take(3).forEach { album ->
                val albumCover = LayoutInflater.from(holder.itemView.context)
                    .inflate(R.layout.album_cover_frame_small, holder.albumCoversContainer, false)
                val imageView = albumCover.findViewById<ImageView>(R.id.album_cover)

                if (album.coverImage.isNotEmpty()) {
                    Glide.with(holder.itemView.context)
                        .load(album.coverImage)
                        .placeholder(R.drawable.album_placeholder)
                        .error(R.drawable.album_placeholder)
                        .into(imageView)
                } else {
                    imageView.setImageResource(R.drawable.album_placeholder)
                }

                holder.albumCoversContainer.addView(albumCover)
            }

            // Set up delete button
            holder.deleteButton.setOnClickListener {
                onListDelete(list)
            }

            holder.itemView.setOnClickListener {
                onListClick(list)
            }
        }

        override fun getItemCount(): Int = lists.size

        fun submitList(newLists: List<UserList>) {
            lists = newLists
            notifyDataSetChanged()
        }
    }
}