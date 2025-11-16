package student.projects.musicreviewapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.models.Music
import student.projects.musicreviewapp.models.UserList

class UserListsFragment : Fragment() {

    private lateinit var listsAdapter: UserListsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_lists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        loadUserLists()
    }

    private fun setupViews(view: View) {
        // Setup back button
        view.findViewById<ImageView>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }

        // Setup plus button (does nothing for now)
        view.findViewById<ImageView>(R.id.plus_button).setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Create list feature coming soon", android.widget.Toast.LENGTH_SHORT).show()
        }

        // Setup RecyclerView
        val listsRecycler = view.findViewById<RecyclerView>(R.id.lists_recycler)
        listsAdapter = UserListsAdapter()

        // Set click listener for lists
        listsAdapter.onListClick = { userList ->
            navigateToListDetail(userList)
        }

        listsRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = listsAdapter
        }
    }

    private fun navigateToListDetail(userList: UserList) {
        val bundle = Bundle().apply {
            putParcelable("userList", userList)
        }
        findNavController().navigate(R.id.action_userListsFragment_to_listDetailFragment, bundle)
    }

    private fun loadUserLists() {
        val mockLists = getMockLists()
        listsAdapter.submitList(mockLists)
    }

    private fun getMockLists(): List<UserList> {
        return listOf(
            UserList(
                id = "1",
                name = "SINNERS",
                creator = "iamnotbhoo",
                description = "WEB-DOING",
                items = getMockAlbums(27)
            ),
            UserList(
                id = "2",
                name = "BRING HER JEANE NEZIEZ",
                creator = "iamnotbhoo",
                description = "i have to die",
                items = getMockAlbums(12)
            ),
            UserList(
                id = "3",
                name = "KIRU OF METAL",
                creator = "iamnotbhoo",
                description = "SOUND OFFICE",
                items = getMockAlbums(8)
            ),
            UserList(
                id = "4",
                name = "LAIAIANI OLOSE",
                creator = "iamnotbhoo",
                description = "Eleanor Sunshine",
                items = getMockAlbums(15)
            )
        )
    }

    private fun getMockAlbums(count: Int): List<Music> {
        return List(count) { index ->
            Music(
                id = "album_$index",
                title = "Album ${index + 1}",
                artist = "Artist ${index + 1}",
                album = "Album ${index + 1}",
                releaseYear = 2025,
                genre = "Various",
                coverImage = "",
                averageRating = 4.0,
                reviewCount = 0
            )
        }
    }

    class UserListsAdapter : RecyclerView.Adapter<UserListsAdapter.ListViewHolder>() {
        private var lists = listOf<UserList>()

        var onListClick: ((UserList) -> Unit)? = null

        class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val listName: TextView = itemView.findViewById(R.id.list_name)
            val listDescription: TextView = itemView.findViewById(R.id.list_description)
            val albumCount: TextView = itemView.findViewById(R.id.album_count)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_user_list, parent, false)
            return ListViewHolder(view)
        }

        override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
            val userList = lists[position]

            holder.listName.text = userList.name
            holder.listDescription.text = userList.description
            holder.albumCount.text = "${userList.items.size} albums"

            holder.itemView.setOnClickListener {
                onListClick?.invoke(userList)
            }
        }

        override fun getItemCount(): Int = lists.size

        fun submitList(newLists: List<UserList>) {
            lists = newLists
            notifyDataSetChanged()
        }
    }
}