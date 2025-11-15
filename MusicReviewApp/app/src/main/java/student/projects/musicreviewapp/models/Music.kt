package student.projects.musicreviewapp.models

import java.util.Date

data class Music(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val releaseYear: Int,
    val genre: String,
    val coverImage: String,
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0
)