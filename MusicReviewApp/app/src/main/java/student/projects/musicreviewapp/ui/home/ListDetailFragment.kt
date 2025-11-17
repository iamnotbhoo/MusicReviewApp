package student.projects.musicreviewapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.auth.ListManager
import student.projects.musicreviewapp.models.UserList
import student.projects.musicreviewapp.models.Music

class ListDetailFragment : Fragment() {

    private lateinit var listManager: ListManager
    private lateinit var currentList: UserList
    private lateinit var albumsAdapter: AlbumsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        listManager = ListManager(requireContext())
        arguments?.let { bundle ->
            currentList = bundle.getParcelable("list") ?: return@let
        }
        return inflater.inflate(R.layout.fragment_list_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        loadListData()
    }

    private fun setupViews(view: View) {
        // Back button
        view.findViewById<ImageView>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }

        // Edit button
        view.findViewById<ImageView>(R.id.edit_button).setOnClickListener {
            // Navigate to edit list screen
            // findNavController().navigate(R.id.editListFragment)
        }

        // Setup albums grid using existing item_album_grid layout
        val albumsRecycler = view.findViewById<RecyclerView>(R.id.albums_recycler)
        albumsRecycler.layoutManager = GridLayoutManager(requireContext(), 2) // Adjust span count as needed
        albumsAdapter = AlbumsAdapter()
        albumsRecycler.adapter = albumsAdapter
    }

    private fun loadListData() {
        requireView().findViewById<TextView>(R.id.list_name).text = currentList.name

        val albumCount = currentList.albums?.size ?: 0
        requireView().findViewById<TextView>(R.id.album_count).text = "$albumCount album${if (albumCount != 1) "s" else ""}"

        // Calculate completion percentage
        val completion = listManager.calculateListCompletion(currentList)
        requireView().findViewById<TextView>(R.id.completion_percentage).text = "$completion%"

        // Load albums
        val albums = currentList.albums ?: emptyList()
        albumsAdapter.submitList(albums)
    }

    private fun navigateToAddEntries() {
        val bundle = Bundle().apply {
            putString("listId", currentList.id)
        }
        findNavController().navigate(R.id.addEntriesFragment, bundle)
    }
    class AlbumsAdapter : RecyclerView.Adapter<AlbumsAdapter.AlbumViewHolder>() {

        private var albums = listOf<Music>()

        class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val albumCover: ImageView = itemView.findViewById(R.id.album_cover) // Using existing ID from item_album_grid
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_album_grid, parent, false) // Using existing layout
            return AlbumViewHolder(view)
        }

        override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
            val album = albums[position]

            // Check if coverImage exists and is not empty
            val coverImage = album.coverImage
            if (!coverImage.isNullOrEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(coverImage)
                    .placeholder(R.drawable.album_placeholder)
                    .error(R.drawable.album_placeholder)
                    .centerCrop()
                    .into(holder.albumCover)
            } else {
                holder.albumCover.setImageResource(R.drawable.album_placeholder)
            }
        }

        override fun getItemCount(): Int = albums.size

        fun submitList(newAlbums: List<Music>) {
            albums = newAlbums
            notifyDataSetChanged()
        }
    }
}