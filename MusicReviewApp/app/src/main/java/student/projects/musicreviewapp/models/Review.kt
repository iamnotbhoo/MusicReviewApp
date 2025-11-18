package student.projects.musicreviewapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Review(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String? = null,
    val content: String = "",
    val timestamp: String = "",
    val musicId: String = "",
    val musicTitle: String = "",
    val musicArtist: String? = null,
    val musicYear: String = "",
    val musicCoverUrl: String? = null,
    val rating: Int = 0,
    val tags: List<String> = emptyList(),
    val isFirstListen: Boolean = true,
    val allowReplies: Boolean = true,
    val liked: Boolean = false,
    val likes: Int = 0
) : Parcelable {
    // Add no-argument constructor for Firestore
    constructor() : this("", "", "", null, "", "", "", "", null, "", null, 0, emptyList(), true, true, false, 0)
}