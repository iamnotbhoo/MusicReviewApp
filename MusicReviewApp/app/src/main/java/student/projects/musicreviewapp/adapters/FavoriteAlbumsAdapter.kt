package student.projects.musicreviewapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.models.Music

class FavoriteAlbumsAdapter : RecyclerView.Adapter<FavoriteAlbumsAdapter.FavoriteAlbumViewHolder>() {

    private var albums = listOf<Music>()
    var onAlbumClick: ((Music, Int) -> Unit)? = null
    var onAddAlbumClick: ((Int) -> Unit)? = null

    class FavoriteAlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val albumCover: ImageView = itemView.findViewById(R.id.album_cover)
        val removeButton: ImageView = itemView.findViewById(R.id.remove_button)
        val addButton: ImageView = itemView.findViewById(R.id.add_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteAlbumViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_album, parent, false)
        return FavoriteAlbumViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteAlbumViewHolder, position: Int) {
        val album = albums[position]

        // Check if this is a placeholder (empty slot)
        val isPlaceholder = album.id.startsWith("placeholder")

        if (isPlaceholder) {
            // Show add button for empty slots
            holder.addButton.visibility = View.VISIBLE
            holder.removeButton.visibility = View.GONE
            holder.albumCover.setImageResource(R.drawable.album_placeholder)

            holder.addButton.setOnClickListener {
                onAddAlbumClick?.invoke(position)
            }

            holder.albumCover.setOnClickListener {
                onAddAlbumClick?.invoke(position)
            }
        } else {
            // Show actual album with remove button
            holder.addButton.visibility = View.GONE
            holder.removeButton.visibility = View.VISIBLE

            // Load album cover from Spotify
            if (album.coverImage.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(album.coverImage)
                    .placeholder(R.drawable.album_placeholder)
                    .error(R.drawable.album_placeholder)
                    .into(holder.albumCover)
            } else {
                holder.albumCover.setImageResource(R.drawable.album_placeholder)
            }

            holder.removeButton.setOnClickListener {
                onAlbumClick?.invoke(album, position)
            }

            holder.albumCover.setOnClickListener {
                // Navigate to album detail
                onAlbumClick?.invoke(album, position)
            }
        }
    }

    override fun getItemCount(): Int = albums.size

    fun submitList(newAlbums: List<Music>) {
        albums = newAlbums
        notifyDataSetChanged()
    }
}