package student.projects.musicreviewapp.components.stories

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import student.projects.musicreviewapp.R

class Story @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var storyImage: ImageView
    private lateinit var storyTitle: TextView
    private lateinit var storyDescription: TextView
    private lateinit var readStoryText: TextView

    var onStoryClick: ((String) -> Unit)? = null
    var onReadStoryClick: ((String) -> Unit)? = null

    private var currentStoryId = ""

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_story, this, true)
        setupViews()
    }

    private fun setupViews() {
        storyImage = findViewById(R.id.story_image)
        storyTitle = findViewById(R.id.story_title)
        storyDescription = findViewById(R.id.story_description)
        readStoryText = findViewById(R.id.read_story_text)

        // Set click listeners
        storyImage.setOnClickListener {
            onStoryClick?.invoke(currentStoryId)
        }

        storyTitle.setOnClickListener {
            onStoryClick?.invoke(currentStoryId)
        }

        readStoryText.setOnClickListener {
            onReadStoryClick?.invoke(currentStoryId)
        }
    }

    fun setStory(story: StoryData) {
        currentStoryId = story.id

        // Load story image
        Glide.with(context)
            .load(story.imageUrl)
            .placeholder(R.drawable.placeholder_music_backdrop)
            .into(storyImage)

        storyTitle.text = story.title
        storyDescription.text = story.description
    }
}