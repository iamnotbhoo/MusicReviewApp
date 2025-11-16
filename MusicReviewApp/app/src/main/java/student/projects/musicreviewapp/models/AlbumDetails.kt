package student.projects.musicreviewapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AlbumDetails(
    val id: String,
    val title: String,
    val artist: String,
    val artists: List<String>,
    val releaseDate: String,
    val formattedReleaseDate: String,
    val releaseDatePrecision: String,
    val totalTracks: Int,
    val totalDurationMs: Long,
    val formattedDuration: String,
    val genres: List<String>,
    val label: String,
    val copyright: String,
    val coverImage: String,
    val largeCoverImage: String,
    val spotifyUrl: String,
    val popularity: Int
) : Parcelable