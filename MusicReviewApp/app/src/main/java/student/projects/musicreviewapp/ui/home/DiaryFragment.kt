package student.projects.musicreviewapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.models.Music
import student.projects.musicreviewapp.models.Review

class DiaryFragment : Fragment() {

    private lateinit var diaryAdapter: DiaryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_diary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        loadDiaryEntries()
    }

    private fun setupViews(view: View) {
        // Setup back button
        view.findViewById<ImageView>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }

        // Setup month header
        view.findViewById<TextView>(R.id.month_year).text = "JULY 2025"

        // Setup RecyclerView
        val diaryRecycler = view.findViewById<RecyclerView>(R.id.diary_recycler)
        diaryAdapter = DiaryAdapter()

        // Set click listener for diary entries
        diaryAdapter.onDiaryEntryClick = { diaryEntry ->
            navigateToReviewDetail(diaryEntry)
        }

        diaryRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = diaryAdapter
        }
    }

    private fun navigateToReviewDetail(diaryEntry: DiaryEntry) {
        val review = Review(
            id = diaryEntry.music.id,
            userId = "1",
            userName = "iamnotbhoo",
            userPhotoUrl = null,
            content = getReviewContentForAlbum(diaryEntry),
            timestamp = "Listened ${diaryEntry.day} ${getMonthName(diaryEntry.month)} ${diaryEntry.year}",
            musicId = diaryEntry.music.id,
            musicTitle = diaryEntry.music.title,
            musicYear = diaryEntry.music.releaseYear.toString(),
            musicCoverUrl = diaryEntry.music.coverImage,
            rating = diaryEntry.userRating,
            tags = emptyList(),
            liked = diaryEntry.isLiked
        )

        val bundle = Bundle().apply {
            putParcelable("review", review)
        }
        findNavController().navigate(R.id.action_diaryFragment_to_reviewDetailFragment, bundle)
    }

    private fun getReviewContentForAlbum(diaryEntry: DiaryEntry): String {
        return when (diaryEntry.music.title) {
            "A Night At The Opera" -> "Classic Queen album with incredible vocal range and production"
            "4:44" -> "Personal and reflective, some of Jay-Z's most honest work"
            "Older" -> "Mature sound with deep emotional resonance"
            "Guard Dog" -> "Raw and emotional indie folk at its finest"
            "JACKBOYS 2" -> "Hard-hitting trap with great features"
            "NEVER ENOUGH" -> "Daniel Caesar delivers soulful R&B perfection"
            "Charm" -> "Catchy pop with great production and vocals"
            "Pray for Paris" -> "Westside Gunn's signature gritty hip-hop"
            else -> "Great listening experience with memorable tracks."
        }
    }

    private fun getMonthName(month: Int): String {
        return when (month) {
            7 -> "July"
            else -> "Unknown"
        }
    }

    private fun loadDiaryEntries() {
        val mockDiaryEntries = getMockDiaryEntries()
        diaryAdapter.submitList(mockDiaryEntries)
    }

    private fun getMockDiaryEntries(): List<DiaryEntry> {
        return listOf(
            DiaryEntry(
                music = Music(
                    id = "1",
                    title = "A Night At The Opera",
                    artist = "Queen",
                    album = "A Night At The Opera",
                    releaseYear = 1975,
                    genre = "Rock",
                    coverImage = "night_at_opera",
                    averageRating = 5.0,
                    reviewCount = 1
                ),
                day = 18,
                month = 7,
                year = 2025,
                userRating = 5,
                hasReview = true,
                isLiked = true
            ),
            DiaryEntry(
                music = Music(
                    id = "2",
                    title = "4:44",
                    artist = "Jay-Z",
                    album = "4:44",
                    releaseYear = 2017,
                    genre = "Hip-Hop",
                    coverImage = "444_jayz",
                    averageRating = 5.0,
                    reviewCount = 1
                ),
                day = 18,
                month = 7,
                year = 2025,
                userRating = 5,
                hasReview = true,
                isLiked = true
            ),
            DiaryEntry(
                music = Music(
                    id = "3",
                    title = "Older",
                    artist = "Ari Lennox",
                    album = "Older",
                    releaseYear = 2024,
                    genre = "R&B",
                    coverImage = "older_ari",
                    averageRating = 5.0,
                    reviewCount = 1
                ),
                day = 15,
                month = 7,
                year = 2025,
                userRating = 5,
                hasReview = true,
                isLiked = false
            ),
            DiaryEntry(
                music = Music(
                    id = "4",
                    title = "Guard Dog",
                    artist = "Searows",
                    album = "Guard Dog",
                    releaseYear = 2022,
                    genre = "Indie Folk",
                    coverImage = "guarddog_searows",
                    averageRating = 5.0,
                    reviewCount = 1
                ),
                day = 14,
                month = 7,
                year = 2025,
                userRating = 5,
                hasReview = true,
                isLiked = true
            ),
            DiaryEntry(
                music = Music(
                    id = "5",
                    title = "JACKBOYS 2",
                    artist = "JACKBOYS",
                    album = "JACKBOYS 2",
                    releaseYear = 2025,
                    genre = "Trap",
                    coverImage = "jackboys_2",
                    averageRating = 3.0,
                    reviewCount = 1
                ),
                day = 12,
                month = 7,
                year = 2025,
                userRating = 3,
                hasReview = false,
                isLiked = false
            ),
            DiaryEntry(
                music = Music(
                    id = "6",
                    title = "NEVER ENOUGH",
                    artist = "Daniel Caesar",
                    album = "NEVER ENOUGH",
                    releaseYear = 2023,
                    genre = "R&B",
                    coverImage = "never_enough",
                    averageRating = 4.0,
                    reviewCount = 1
                ),
                day = 10,
                month = 7,
                year = 2025,
                userRating = 4,
                hasReview = false,
                isLiked = true
            ),
            DiaryEntry(
                music = Music(
                    id = "7",
                    title = "Charm",
                    artist = "Rema",
                    album = "Charm",
                    releaseYear = 2024,
                    genre = "Afrobeats",
                    coverImage = "charm_rema",
                    averageRating = 5.0,
                    reviewCount = 1
                ),
                day = 7,
                month = 7,
                year = 2025,
                userRating = 5,
                hasReview = true,
                isLiked = false
            ),
            DiaryEntry(
                music = Music(
                    id = "8",
                    title = "Pray for Paris",
                    artist = "Westside Gunn",
                    album = "Pray for Paris",
                    releaseYear = 2020,
                    genre = "Hip-Hop",
                    coverImage = "pray_for_paris",
                    averageRating = 4.0,
                    reviewCount = 1
                ),
                day = 5,
                month = 7,
                year = 2025,
                userRating = 4,
                hasReview = false,
                isLiked = true
            )
        )
    }

    data class DiaryEntry(
        val music: Music,
        val day: Int,
        val month: Int,
        val year: Int,
        val userRating: Int,
        val hasReview: Boolean,
        val isLiked: Boolean
    )

    class DiaryAdapter : RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder>() {
        private var diaryEntries = listOf<DiaryEntry>()

        var onDiaryEntryClick: ((DiaryEntry) -> Unit)? = null

        class DiaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val dayNumber: TextView = itemView.findViewById(R.id.day_number)
            val albumTitle: TextView = itemView.findViewById(R.id.album_title)
            val albumYear: TextView = itemView.findViewById(R.id.album_year)
            val star1: ImageView = itemView.findViewById(R.id.star1)
            val star2: ImageView = itemView.findViewById(R.id.star2)
            val star3: ImageView = itemView.findViewById(R.id.star3)
            val star4: ImageView = itemView.findViewById(R.id.star4)
            val star5: ImageView = itemView.findViewById(R.id.star5)
            val reviewIcon: ImageView = itemView.findViewById(R.id.review_icon)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_diary_entry, parent, false)
            return DiaryViewHolder(view)
        }

        override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
            val diaryEntry = diaryEntries[position]
            val music = diaryEntry.music

            // Set day number
            holder.dayNumber.text = diaryEntry.day.toString()

            // Set album info
            holder.albumTitle.text = music.title
            holder.albumYear.text = "${music.releaseYear}"

            // Update star ratings
            updateStarRating(holder, diaryEntry.userRating)

            // Update review icon (show if user has written a review)
            holder.reviewIcon.visibility = if (diaryEntry.hasReview) View.VISIBLE else View.GONE

            // Set click listener
            holder.itemView.setOnClickListener {
                onDiaryEntryClick?.invoke(diaryEntry)
            }
        }

        private fun updateStarRating(holder: DiaryViewHolder, rating: Int) {
            val stars = listOf(holder.star1, holder.star2, holder.star3, holder.star4, holder.star5)
            val activeColor = ContextCompat.getColor(holder.itemView.context, R.color.purple_500)
            val inactiveColor = ContextCompat.getColor(holder.itemView.context, R.color.gray_400)

            stars.forEachIndexed { index, star ->
                val color = if (index < rating) activeColor else inactiveColor
                ImageViewCompat.setImageTintList(star, android.content.res.ColorStateList.valueOf(color))
            }
        }

        override fun getItemCount(): Int = diaryEntries.size

        fun submitList(newEntries: List<DiaryEntry>) {
            diaryEntries = newEntries
            notifyDataSetChanged()
        }
    }
}