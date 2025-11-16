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
import student.projects.musicreviewapp.models.Music
import student.projects.musicreviewapp.models.UserList

class ListDetailFragment : Fragment() {

    private lateinit var userList: UserList
    private lateinit var albumsAdapter: ListAlbumsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            userList = bundle.getParcelable("userList") ?: return@let
        }

        setupViews(view)
        loadListData()
    }

    private fun setupViews(view: View) {
        // Setup back button
        view.findViewById<ImageView>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }

        // Setup reply button
        view.findViewById<TextView>(R.id.reply_button).setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Reply feature coming soon", android.widget.Toast.LENGTH_SHORT).show()
        }

        // Setup RecyclerView
        val albumsRecycler = view.findViewById<RecyclerView>(R.id.albums_recycler)
        albumsAdapter = ListAlbumsAdapter()

        // Set click listener for albums
        albumsAdapter.onAlbumClick = { music ->
            navigateToAlbumDetail(music)
        }

        albumsRecycler.apply {
            layoutManager = GridLayoutManager(requireContext(), 3) // 3 columns grid
            adapter = albumsAdapter
        }
    }

    private fun navigateToAlbumDetail(music: Music) {
        val bundle = Bundle().apply {
            putParcelable("album", music)
        }
        findNavController().navigate(R.id.action_listDetailFragment_to_albumDetailFragment, bundle)
    }

    private fun loadListData() {
        // Update header info
        view?.let { v ->
            v.findViewById<TextView>(R.id.list_name).text = userList.name
            v.findViewById<TextView>(R.id.list_description).text = userList.description
            v.findViewById<TextView>(R.id.album_count).text = "${userList.items.size} albums"

            // Calculate completion percentage (mock data - in real app, track user progress)
            val completionPercentage = calculateCompletionPercentage()
            v.findViewById<TextView>(R.id.completion_percentage).text = "$completionPercentage%"
        }

        // Load albums
        albumsAdapter.submitList(userList.items.take(9)) // Show first 9 albums in grid
    }

    private fun calculateCompletionPercentage(): Int {
        // Mock completion calculation - in real app, check how many albums user has listened to
        return when (userList.name) {
            "SINNERS" -> 100
            "BRING HER JEANE NEZIEZ" -> 75
            "KIRU OF METAL" -> 50
            "LAIAIANI OLOSE" -> 25
            else -> 0
        }
    }

    class ListAlbumsAdapter : RecyclerView.Adapter<ListAlbumsAdapter.AlbumViewHolder>() {
        private var albums = listOf<Music>()

        var onAlbumClick: ((Music) -> Unit)? = null

        class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val albumCover: ImageView = itemView.findViewById(R.id.album_cover)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_album, parent, false)
            return AlbumViewHolder(view)
        }

        override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
            val music = albums[position]

            // Load album cover
            if (!music.coverImage.isNullOrEmpty()) {
                try {
                    val resourceId = holder.itemView.context.resources.getIdentifier(
                        music.coverImage,
                        "drawable",
                        holder.itemView.context.packageName
                    )
                    if (resourceId != 0) {
                        Glide.with(holder.itemView.context)
                            .load(resourceId)
                            .placeholder(R.drawable.album_placeholder)
                            .error(R.drawable.album_placeholder)
                            .into(holder.albumCover)
                    } else {
                        holder.albumCover.setImageResource(R.drawable.album_placeholder)
                    }
                } catch (e: Exception) {
                    holder.albumCover.setImageResource(R.drawable.album_placeholder)
                }
            } else {
                holder.albumCover.setImageResource(R.drawable.album_placeholder)
            }

            holder.itemView.setOnClickListener {
                onAlbumClick?.invoke(music)
            }
        }

        override fun getItemCount(): Int = albums.size

        fun submitList(newAlbums: List<Music>) {
            albums = newAlbums
            notifyDataSetChanged()
        }
    }
}