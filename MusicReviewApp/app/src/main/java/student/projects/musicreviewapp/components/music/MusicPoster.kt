package student.projects.musicreviewapp.components.music

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.components.buttons.FavouriteButton
import student.projects.musicreviewapp.components.buttons.ListenButton

class MusicPoster @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var posterImage: ImageView
    private lateinit var favouriteButton: FavouriteButton
    private lateinit var listenButton: ListenButton
    private lateinit var buttonsContainer: LinearLayout

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private var musicId = ""
    private var musicTitle = ""

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_music_poster, this, true)
        setupViews()
        setupHoverEffects()
    }

    private fun setupViews() {
        posterImage = findViewById(R.id.music_poster_image)
        favouriteButton = findViewById(R.id.favourite_button)
        listenButton = findViewById(R.id.listen_button)
        buttonsContainer = findViewById(R.id.buttons_container)
    }

    private fun setupHoverEffects() {
        // Show buttons on hover/touch
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

    fun setMusicData(id: String, title: String, posterUrl: String?) {
        musicId = id
        musicTitle = title

        // Load poster image
        if (posterUrl.isNullOrEmpty()) {
            posterImage.setImageResource(R.drawable.ic_music_note)
        } else {
            Glide.with(context)
                .load(posterUrl)
                .placeholder(R.drawable.ic_music_note)
                .into(posterImage)
        }

        // Setup buttons
        favouriteButton.setMusicData(id, title, false) // You'll need to fetch actual state from Firestore
        listenButton.setMusicData(id, title, false) // You'll need to fetch actual state from Firestore
    }
}