package student.projects.musicreviewapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Music(
    val id: String = "",
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val releaseYear: Int = 0,
    val genre: String = "",
    val coverImage: String = "",
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0
) : Parcelable {
    // Add no-argument constructor for Firestore
    constructor() : this("", "", "", "", 0, "", "", 0.0, 0)
}