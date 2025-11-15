package student.projects.musicreviewapp.models

data class User(
    val id: String,
    val username: String,
    val email: String,
    val profilePicture: String? = null,
    val bio: String? = null
)