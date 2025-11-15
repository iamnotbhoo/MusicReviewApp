package student.projects.musicreviewapp.components.profile

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.components.buttons.FavouriteButton
import student.projects.musicreviewapp.components.buttons.ListenButton
import student.projects.musicreviewapp.models.Music

class ProfileMusicPoster @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var posterImage: ImageView
    private lateinit var favouriteButton: FavouriteButton
    private lateinit var listenButton: ListenButton
    private lateinit var buttonsContainer: LinearLayout

    private var musicId = ""
    private var musicTitle = ""

    var onMusicClick: ((String) -> Unit)? = null
    var onEvent: (() -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_profile_music_poster, this, true)
        setupViews()
        setupHoverEffects()
    }

    private fun setupViews() {
        posterImage = findViewById(R.id.profile_poster_image)
        favouriteButton = findViewById(R.id.profile_favourite_button)
        listenButton = findViewById(R.id.profile_listen_button)
        buttonsContainer = findViewById(R.id.profile_buttons_container)

        posterImage.setOnClickListener {
            onMusicClick?.invoke(musicId)
        }
    }

    private fun setupHoverEffects() {
        setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    buttonsContainer.visibility = View.VISIBLE
                    true
                }
                android.view.MotionEvent.ACTION_UP,
                android.view.MotionEvent.ACTION_CANCEL -> {
                    buttonsContainer.visibility = View.GONE
                    true
                }
                else -> false
            }
        }
    }

    fun setMusicData(music: Music, isFavourite: Boolean, isListened: Boolean) {
        musicId = music.id
        musicTitle = music.title

        // Load poster image
        if (music.coverImage.isNotEmpty()) {
            Glide.with(context)
                .load(music.coverImage)
                .placeholder(R.drawable.ic_music_note)
                .into(posterImage)
        } else {
            posterImage.setImageResource(R.drawable.ic_music_note)
        }

        // Setup buttons
        favouriteButton.setMusicData(music.id, music.title, isFavourite)
        listenButton.setMusicData(music.id, music.title, isListened)

        favouriteButton.onEvent = {
            onEvent?.invoke()
        }
        listenButton.onEvent = {
            onEvent?.invoke()
        }
    }
}