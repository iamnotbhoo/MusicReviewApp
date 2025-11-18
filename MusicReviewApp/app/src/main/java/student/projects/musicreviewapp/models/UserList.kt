package student.projects.musicreviewapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserList(
    val id: String,
    val name: String,
    val description: String,
    val albums: MutableList<Music>,
    val tags: List<String>,
    val createdAt: String,
    val creator: String,
    val isPublic: Boolean,
    val likes: Int,
    val liked: Boolean // Add this field
) : Parcelable {
    // No-argument constructor for Firestore
    constructor() : this("", "", "", mutableListOf(), emptyList(), "", "", true, 0, false)
}