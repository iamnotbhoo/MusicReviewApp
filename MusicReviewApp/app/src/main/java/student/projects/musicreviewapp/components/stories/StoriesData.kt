package student.projects.musicreviewapp.components.stories

data class StoryData(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val link: String = ""
)

object StoriesData {
    val stories = listOf(
        StoryData(
            id = "0",
            title = "10 Best Music Albums of 2023",
            description = "Discover the top music albums that defined the year 2023 across various genres and artists.",
            imageUrl = "https://example.com/story1.jpg"
        ),
        StoryData(
            id = "1",
            title = "Rising Artists to Watch",
            description = "Explore the most promising new artists making waves in the music industry this year.",
            imageUrl = "https://example.com/story2.jpg"
        ),
        StoryData(
            id = "2",
            title = "Genre Evolution: From Jazz to Electronic",
            description = "Trace the fascinating evolution of music genres and their influence on modern sounds.",
            imageUrl = "https://example.com/story3.jpg"
        ),
        StoryData(
            id = "3",
            title = "Behind the Lyrics: Songwriting Secrets",
            description = "Dive into the creative process behind some of the most iconic songs in music history.",
            imageUrl = "https://example.com/story4.jpg"
        ),
        StoryData(
            id = "4",
            title = "Music Production Techniques",
            description = "Learn about the latest production techniques shaping today's music landscape.",
            imageUrl = "https://example.com/story5.jpg"
        ),
        StoryData(
            id = "5",
            title = "Live Performance Masterpieces",
            description = "Relive the most unforgettable live performances that captivated audiences worldwide.",
            imageUrl = "https://example.com/story6.jpg"
        )
    )
}