package student.projects.musicreviewapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserList(
    val id: String,
    val name: String,
    val description: String = "",
    val albums: List<Music> = emptyList(),
    val tags: List<String> = emptyList(),
    val createdAt: String = "",
    val isPublic: Boolean = true,
    val creator: String = "",
    val likes: Int = 0,
    val liked: Boolean = false
) : Parcelable