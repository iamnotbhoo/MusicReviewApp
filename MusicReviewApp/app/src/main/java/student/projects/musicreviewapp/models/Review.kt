package student.projects.musicreviewapp.models

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
    val musicCoverUrl: String? = null
)