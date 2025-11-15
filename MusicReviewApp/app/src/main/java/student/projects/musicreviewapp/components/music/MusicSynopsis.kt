package student.projects.musicreviewapp.components.music

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import student.projects.musicreviewapp.R

class MusicSynopsis @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var titleText: TextView
    private lateinit var artistText: TextView
    private lateinit var yearText: TextView
    private lateinit var albumText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var musicCast: MusicCast

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_music_synopsis, this, true)
        setupViews()
    }

    private fun setupViews() {
        titleText = findViewById(R.id.music_title)
        artistText = findViewById(R.id.music_artist)
        yearText = findViewById(R.id.music_year)
        albumText = findViewById(R.id.music_album)
        descriptionText = findViewById(R.id.music_description)
        musicCast = findViewById(R.id.music_cast)
    }

    fun setMusicData(
        title: String,
        artist: String,
        year: Int?,
        album: String?,
        description: String?,
        featuredArtists: List<String> = emptyList()
    ) {
        titleText.text = title
        artistText.text = artist
        yearText.text = year?.toString() ?: "Unknown"
        albumText.text = album ?: "Single"
        descriptionText.text = description ?: "No description available"

        musicCast.setCast(featuredArtists)
    }
}