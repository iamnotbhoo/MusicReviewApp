package student.projects.musicreviewapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
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
) : Parcelable