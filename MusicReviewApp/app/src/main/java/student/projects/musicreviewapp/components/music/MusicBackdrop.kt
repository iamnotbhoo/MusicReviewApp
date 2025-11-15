package student.projects.musicreviewapp.components.music

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import student.projects.musicreviewapp.R

class MusicBackdrop @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var backdropImage: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_music_backdrop, this, true)
        setupViews()
    }

    private fun setupViews() {
        backdropImage = findViewById(R.id.music_backdrop_image)
    }

    fun setBackdrop(imageUrl: String?) {
        if (imageUrl.isNullOrEmpty()) {
            backdropImage.setImageResource(R.drawable.placeholder_music_backdrop)
        } else {
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_music_backdrop)
                .into(backdropImage)
        }
    }
}