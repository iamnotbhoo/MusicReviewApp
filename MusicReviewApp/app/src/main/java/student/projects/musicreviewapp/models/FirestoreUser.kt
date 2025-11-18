package student.projects.musicreviewapp.models

data class FirestoreUser(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val profilePicture: String? = null,
    val bio: String = "trackd",
    val createdAt: Long = System.currentTimeMillis(),
    val favoriteAlbums: List<String> = emptyList(),
    val reviewedAlbums: List<String> = emptyList(),
    val createdLists: List<String> = emptyList(),
    val likedAlbums: List<String> = emptyList(),
    val likedReviews: List<String> = emptyList(),
    val likedLists: List<String> = emptyList(),
    val playlistAlbums: List<String> = emptyList(),
    val favoriteAlbumsFull: List<Map<String, Any>> = emptyList()
) {
    // Add no-argument constructor for Firestore
    constructor() : this("", "", "", null, "trackd", System.currentTimeMillis(),
        emptyList(), emptyList(), emptyList(), emptyList(),
        emptyList(), emptyList(), emptyList(), emptyList())
}