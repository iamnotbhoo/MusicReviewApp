package student.projects.musicreviewapp.components.letsyou

import student.projects.musicreviewapp.R

data class LetsYouCardData(
    val id: String,
    val iconRes: Int,
    val text: String,
    val bgColor: String
)

object LetsYouData {
    val cards = listOf(
        LetsYouCardData(
            id = "0",
            iconRes = R.drawable.ic_music_note,
            text = "Keep track of every song you've ever listened to (or just start from the day you join)",
            bgColor = "#00C030"
        ),
        LetsYouCardData(
            id = "1",
            iconRes = R.drawable.ic_favorite_filled,
            text = "Show some love for your favorite songs, lists and reviews with a 'like'",
            bgColor = "#EE7000"
        ),
        LetsYouCardData(
            id = "2",
            iconRes = R.drawable.ic_review,
            text = "Write and share reviews, and follow friends and other members to read theirs",
            bgColor = "#209CE4"
        ),
        LetsYouCardData(
            id = "3",
            iconRes = R.drawable.ic_star,
            text = "Rate each song on a five-star scale (with halves) to record and share your reaction",
            bgColor = "#00C030"
        ),
        LetsYouCardData(
            id = "4",
            iconRes = R.drawable.ic_diary,
            text = "Keep a diary of your music listening (and upgrade to PRO for comprehensive stats)",
            bgColor = "#EE7000"
        ),
        LetsYouCardData(
            id = "5",
            iconRes = R.drawable.ic_list,
            text = "Compile and share lists of songs on any topic and keep a playlist of songs to hear",
            bgColor = "#209CE4"
        )
    )
}