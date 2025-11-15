package student.projects.musicreviewapp.components.review

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.models.Music
import student.projects.musicreviewapp.models.Review as ReviewModel

class MusicReviewExtended @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var musicPoster: ImageView
    private lateinit var musicTitle: TextView
    private lateinit var timestamp: TextView
    private lateinit var reviewContent: TextView

    var onMusicClick: ((String) -> Unit)? = null

    private var currentMusic: Music? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_music_review_extended, this, true)
        setupViews()
    }

    private fun setupViews() {
        musicPoster = findViewById(R.id.extended_music_poster)
        musicTitle = findViewById(R.id.extended_music_title)
        timestamp = findViewById(R.id.extended_timestamp)
        reviewContent = findViewById(R.id.extended_review_content)

        musicPoster.setOnClickListener {
            currentMusic?.id?.let { musicId ->
                onMusicClick?.invoke(musicId)
            }
        }

        musicTitle.setOnClickListener {
            currentMusic?.id?.let { musicId ->
                onMusicClick?.invoke(musicId)
            }
        }
    }

    fun setReview(review: ReviewModel, music: Music?) {
        currentMusic = music

        // Load music poster
        if (music != null && !music.coverImage.isNullOrEmpty()) {
            Glide.with(context)
                .load(music.coverImage)
                .placeholder(R.drawable.ic_music_note)
                .into(musicPoster)
        } else {
            musicPoster.setImageResource(R.drawable.ic_music_note)
        }

        musicTitle.text = music?.title ?: "Unknown Music"
        timestamp.text = review.timestamp
        reviewContent.text = review.content
    }
}