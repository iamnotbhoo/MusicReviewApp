package student.projects.musicreviewapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class Playlist(
    val id: String,
    val title: String,
    val creator: String,
    val description: String,
    val itemCount: Int = 0,
    val coverImage: String? = null
)

