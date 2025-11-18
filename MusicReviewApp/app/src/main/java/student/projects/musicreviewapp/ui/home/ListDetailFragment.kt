package student.projects.musicreviewapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.auth.FirebaseLikeManager
import student.projects.musicreviewapp.auth.FirebaseListManager
import student.projects.musicreviewapp.models.UserList
import student.projects.musicreviewapp.models.Music

class ListDetailFragment : Fragment() {

    private lateinit var userList: UserList
    private lateinit var listManager: FirebaseListManager
    private lateinit var likeManager: FirebaseLikeManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        listManager = FirebaseListManager(requireContext())
        likeManager = FirebaseLikeManager(requireContext())
        return inflater.inflate(R.layout.fragment_list_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            userList = bundle.getParcelable("list") ?: return@let
        }

        setupViews(view)
        setupBackButton(view)
        setupLikeButton(view)
        setupAlbumsRecycler(view)
    }

    private fun setupViews(view: View) {
        // Set list info
        view.findViewById<TextView>(R.id.list_title).text = userList.name
        view.findViewById<TextView>(R.id.list_creator).text = "by ${userList.creator}"
        view.findViewById<TextView>(R.id.list_description).text = userList.description

        // Set likes count
        view.findViewById<TextView>(R.id.likes_count).text = "${userList.likes} likes"

        // Set creation date if available
        val createdAtView = view.findViewById<TextView>(R.id.created_date)
        createdAtView?.text = "Created ${userList.createdAt}"
    }

    private fun setupBackButton(view: View) {
        view.findViewById<ImageView>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupLikeButton(view: View) {
        val likeButton = view.findViewById<ImageView>(R.id.like_icon)
        val likeCountText = view.findViewById<TextView>(R.id.likes_count)

        // Set initial state - check if list is liked
        likeManager.isListLiked(userList.id) { isLiked ->
            activity?.runOnUiThread {
                updateLikeButton(isLiked, userList.likes, likeButton, likeCountText)
            }
        }

        likeButton.setOnClickListener {
            likeManager.isListLiked(userList.id) { isCurrentlyLiked ->
                activity?.runOnUiThread {
                    if (isCurrentlyLiked) {
                        // Unlike the list
                        likeManager.unlikeList(userList.id) { success ->
                            if (success) {
                                listManager.unlikeList(userList.id) { listSuccess ->
                                    activity?.runOnUiThread {
                                        val newLikes = maxOf(0, userList.likes - 1)
                                        updateLikeButton(false, newLikes, likeButton, likeCountText)
                                        showToast("List unliked")
                                    }
                                }
                            }
                        }
                    } else {
                        // Like the list
                        likeManager.likeList(userList) { success ->
                            if (success) {
                                listManager.likeList(userList.id) { listSuccess ->
                                    activity?.runOnUiThread {
                                        val newLikes = userList.likes + 1
                                        updateLikeButton(true, newLikes, likeButton, likeCountText)
                                        showToast("List liked")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateLikeButton(isLiked: Boolean, likeCount: Int, likeButton: ImageView, likeCountText: TextView) {
        if (isLiked) {
            likeButton.setImageResource(R.drawable.ic_heart_orange)
            likeButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.orange_500))
        } else {
            likeButton.setImageResource(R.drawable.ic_heart)
            likeButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.grey_400))
        }
        likeCountText.text = "$likeCount likes"
    }

    private fun setupAlbumsRecycler(view: View) {
        val albumsRecycler = view.findViewById<RecyclerView>(R.id.albums_recycler)
        albumsRecycler.layoutManager = LinearLayoutManager(requireContext())

        val adapter = ListAlbumsAdapter(userList.albums) { album ->
            navigateToAlbumDetail(album)
        }
        albumsRecycler.adapter = adapter
    }

    private fun navigateToAlbumDetail(album: Music) {
        val bundle = Bundle().apply {
            putParcelable("album", album)
        }
        findNavController().navigate(R.id.action_listDetailFragment_to_albumDetailFragment, bundle)
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }
}

// Adapter for displaying albums in the list
class ListAlbumsAdapter(
    private val albums: List<Music>,
    private val onAlbumClick: (Music) -> Unit
) : RecyclerView.Adapter<ListAlbumsAdapter.AlbumViewHolder>() {

    class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val albumCover: ImageView = itemView.findViewById(R.id.album_cover)
        val albumTitle: TextView = itemView.findViewById(R.id.album_title)
        val artistName: TextView = itemView.findViewById(R.id.artist_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_album, parent, false)
        return AlbumViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = albums[position]

        // Load album cover
        if (album.coverImage.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(album.coverImage)
                .placeholder(R.drawable.album_placeholder)
                .error(R.drawable.album_placeholder)
                .into(holder.albumCover)
        } else {
            holder.albumCover.setImageResource(R.drawable.album_placeholder)
        }

        holder.albumTitle.text = album.title
        holder.artistName.text = album.artist

        holder.itemView.setOnClickListener {
            onAlbumClick(album)
        }
    }

    override fun getItemCount(): Int = albums.size
}