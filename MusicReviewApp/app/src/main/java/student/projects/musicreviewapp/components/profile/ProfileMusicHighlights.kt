package student.projects.musicreviewapp.components.profile

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.models.Music
import student.projects.musicreviewapp.models.User

class ProfileMusicHighlights @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var titleText: TextView
    private lateinit var seeAllText: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var noMusicText: TextView

    private val adapter = ProfileMusicAdapter(emptyList())

    var onSeeAll: (() -> Unit)? = null
    var onMusicClick: ((String) -> Unit)? = null
    var onEvent: (() -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_profile_music_highlights, this, true)
        setupViews()
    }

    private fun setupViews() {
        titleText = findViewById(R.id.highlights_title)
        seeAllText = findViewById(R.id.see_all_text)
        recyclerView = findViewById(R.id.highlights_recycler_view)
        noMusicText = findViewById(R.id.no_music_text)

        recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2) // 2 columns for profile view
            adapter = this@ProfileMusicHighlights.adapter
        }

        seeAllText.setOnClickListener {
            onSeeAll?.invoke()
        }

        adapter.onMusicClick = { musicId ->
            onMusicClick?.invoke(musicId)
        }
        adapter.onEvent = {
            onEvent?.invoke()
        }
    }

    fun setHighlights(
        type: String,
        musicList: List<Music>,
        favourites: List<String>,
        listened: List<String>
    ) {
        titleText.text = "${type.uppercase()} MUSIC"

        if (musicList.isEmpty()) {
            noMusicText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            noMusicText.text = "This user has no $type music yet."
        } else {
            noMusicText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.updateData(musicList, favourites, listened)
        }
    }
}

class ProfileMusicAdapter(
    private var musicList: List<Music>,
    private var favourites: List<String> = emptyList(),
    private var listened: List<String> = emptyList()
) : RecyclerView.Adapter<ProfileMusicAdapter.MusicViewHolder>() {

    var onMusicClick: ((String) -> Unit)? = null
    var onEvent: (() -> Unit)? = null

    class MusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val poster: ProfileMusicPoster = itemView.findViewById(R.id.profile_music_poster)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile_music, parent, false)
        return MusicViewHolder(view)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val music = musicList[position]
        val isFavourite = favourites.contains(music.id)
        val isListened = listened.contains(music.id)

        holder.poster.setMusicData(music, isFavourite, isListened)
        holder.poster.onMusicClick = { musicId ->
            onMusicClick?.invoke(musicId)
        }
        holder.poster.onEvent = {
            onEvent?.invoke()
        }
    }

    override fun getItemCount(): Int = musicList.size

    fun updateData(newMusicList: List<Music>, newFavourites: List<String>, newListened: List<String>) {
        musicList = newMusicList
        favourites = newFavourites
        listened = newListened
        notifyDataSetChanged()
    }
}