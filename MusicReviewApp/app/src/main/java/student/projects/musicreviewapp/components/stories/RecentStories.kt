package student.projects.musicreviewapp.components.stories

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import student.projects.musicreviewapp.R

class RecentStories @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var storiesRecyclerView: RecyclerView
    private lateinit var allStoriesText: TextView

    private val adapter = StoriesAdapter(StoriesData.stories)

    var onAllStoriesClick: (() -> Unit)? = null
    var onStoryClick: ((String) -> Unit)? = null
    var onReadStoryClick: ((String) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_recent_stories, this, true)
        setupViews()
    }

    private fun setupViews() {
        storiesRecyclerView = findViewById(R.id.stories_recycler_view)
        allStoriesText = findViewById(R.id.all_stories_text)

        // Use horizontal layout for mobile, grid for tablet
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        storiesRecyclerView.layoutManager = layoutManager
        storiesRecyclerView.adapter = adapter

        allStoriesText.setOnClickListener {
            onAllStoriesClick?.invoke()
        }

        adapter.onStoryClick = { storyId: String ->
            onStoryClick?.invoke(storyId)
        }

        adapter.onReadStoryClick = { storyId: String ->
            onReadStoryClick?.invoke(storyId)
        }
    }
}

class StoriesAdapter(private var stories: List<StoryData>) :
    RecyclerView.Adapter<StoriesAdapter.StoryViewHolder>() {

    var onStoryClick: ((String) -> Unit)? = null
    var onReadStoryClick: ((String) -> Unit)? = null

    class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val story: Story = itemView.findViewById(R.id.story_component)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_story, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val storyData = stories[position]
        holder.story.setStory(storyData)
        holder.story.onStoryClick = { storyId: String ->
            onStoryClick?.invoke(storyId)
        }
        holder.story.onReadStoryClick = { storyId: String ->
            onReadStoryClick?.invoke(storyId)
        }
    }

    override fun getItemCount(): Int = stories.size

    fun updateData(newStories: List<StoryData>) {
        stories = newStories
        notifyDataSetChanged()
    }
}