package student.projects.musicreviewapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.models.Music
import com.bumptech.glide.Glide

class MusicAdapter(private var musicList: List<Music>) :
    RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    private var onItemClickListener: ((Music) -> Unit)? = null

    class MusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coverImage: ImageView = itemView.findViewById(R.id.cover_image)
        val title: TextView = itemView.findViewById(R.id.music_title)
        val artist: TextView = itemView.findViewById(R.id.artist_name)
        val rating: RatingBar = itemView.findViewById(R.id.rating_bar)
        val reviewCount: TextView = itemView.findViewById(R.id.review_count)
        val overview: TextView? = itemView.findViewById(R.id.music_overview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_music, parent, false)
        return MusicViewHolder(view)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val music = musicList[position]

        // Display album title and artist
        holder.title.text = music.title // This should be the album name
        holder.artist.text = music.artist // This is the artist name
        holder.rating.rating = (music.averageRating / 2).toFloat()
        holder.reviewCount.text = "${music.reviewCount} reviews"

        // Show overview if available (for filter results)
        holder.overview?.text = music.album ?: "No description available"

        // Load album cover using Glide - FIXED: Make sure we're loading the image
        if (music.coverImage.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(music.coverImage)
                .placeholder(R.drawable.album_placeholder)
                .error(R.drawable.album_placeholder)
                .into(holder.coverImage)
        } else {
            // If no cover image, use placeholder
            holder.coverImage.setImageResource(R.drawable.album_placeholder)
        }

        // Set click listener
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(music)
        }
    }

    override fun getItemCount(): Int = musicList.size

    fun updateData(newMusicList: List<Music>) {
        musicList = newMusicList
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (Music) -> Unit) {
        onItemClickListener = listener
    }
}