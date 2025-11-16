package student.projects.musicreviewapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Review(
    val id: String,
    val userId: String,
    val userName: String,
    val userPhotoUrl: String?,
    val content: String,
    val timestamp: String = "",
    val musicId: String = "",
    val musicTitle: String = "",
    val musicYear: String = "",
    val musicCoverUrl: String? = null,
    // NEW FIELDS FOR REVIEW SYSTEM
    val rating: Int = 0,
    val tags: List<String> = emptyList(),
    val isFirstListen: Boolean = true,
    val allowReplies: Boolean = true,
    val liked: Boolean = false
) : Parcelable