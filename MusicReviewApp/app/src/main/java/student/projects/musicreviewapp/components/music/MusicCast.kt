package student.projects.musicreviewapp.components.music

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

class MusicCast @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var castRecyclerView: RecyclerView
    private lateinit var noCastText: TextView

    private val castAdapter = CastAdapter(emptyList())

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_music_cast, this, true)
        setupViews()
    }

    private fun setupViews() {
        castRecyclerView = findViewById(R.id.cast_recycler_view)
        noCastText = findViewById(R.id.no_cast_text)

        castRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = castAdapter
        }
    }

    fun setCast(artists: List<String>) {
        if (artists.isEmpty()) {
            noCastText.visibility = View.VISIBLE
            castRecyclerView.visibility = View.GONE
        } else {
            noCastText.visibility = View.GONE
            castRecyclerView.visibility = View.VISIBLE
            castAdapter.updateData(artists)
        }
    }
}

class CastAdapter(private var artists: List<String>) :
    RecyclerView.Adapter<CastAdapter.CastViewHolder>() {

    class CastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val artistName: TextView = itemView.findViewById(R.id.artist_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cast, parent, false)
        return CastViewHolder(view)
    }

    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        holder.artistName.text = artists[position]
    }

    override fun getItemCount(): Int = artists.size

    fun updateData(newArtists: List<String>) {
        artists = newArtists
        notifyDataSetChanged()
    }
}