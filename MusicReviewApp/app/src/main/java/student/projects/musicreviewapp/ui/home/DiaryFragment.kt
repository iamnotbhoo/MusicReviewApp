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
import student.projects.musicreviewapp.auth.FirebaseReviewManager

// Move DiaryEntry data class outside of DiaryFragment
data class DiaryEntry(
    val music: Music,
    val day: Int,
    val month: Int,
    val year: Int,
    val userRating: Int,
    val hasReview: Boolean,
    val isLiked: Boolean,
    val reviewId: String,
    val reviewContent: String
)

class DiaryFragment : Fragment() {

    private lateinit var diaryAdapter: DiaryAdapter
    private lateinit var reviewManager: FirebaseReviewManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_diary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reviewManager = FirebaseReviewManager(requireContext())

        setupViews(view)
        loadDiaryEntries()
    }

    private fun setupViews(view: View) {
        // Setup back button
        view.findViewById<ImageView>(R.id.back_button).setOnClickListener {
            findNavController().popBackStack()
        }

        // Setup month header - you can make this dynamic based on current month
        view.findViewById<TextView>(R.id.month_year).text = getCurrentMonthYear()

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

    private fun getCurrentMonthYear(): String {
        val calendar = java.util.Calendar.getInstance()
        val month = when (calendar.get(java.util.Calendar.MONTH)) {
            java.util.Calendar.JULY -> "JULY"
            java.util.Calendar.AUGUST -> "AUGUST"
            java.util.Calendar.SEPTEMBER -> "SEPTEMBER"
            java.util.Calendar.OCTOBER -> "OCTOBER"
            java.util.Calendar.NOVEMBER -> "NOVEMBER"
            java.util.Calendar.DECEMBER -> "DECEMBER"
            java.util.Calendar.JANUARY -> "JANUARY"
            java.util.Calendar.FEBRUARY -> "FEBRUARY"
            java.util.Calendar.MARCH -> "MARCH"
            java.util.Calendar.APRIL -> "APRIL"
            java.util.Calendar.MAY -> "MAY"
            java.util.Calendar.JUNE -> "JUNE"
            else -> "UNKNOWN"
        }
        val year = calendar.get(java.util.Calendar.YEAR)
        return "$month $year"
    }

    private fun navigateToReviewDetail(diaryEntry: DiaryEntry) {
        val review = Review(
            id = diaryEntry.reviewId,
            userId = "1",
            userName = "iamnotbhoo",
            userPhotoUrl = null,
            content = diaryEntry.reviewContent,
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

    private fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "January"
            2 -> "February"
            3 -> "March"
            4 -> "April"
            5 -> "May"
            6 -> "June"
            7 -> "July"
            8 -> "August"
            9 -> "September"
            10 -> "October"
            11 -> "November"
            12 -> "December"
            else -> "Unknown"
        }
    }

    private fun loadDiaryEntries() {
        // Get actual reviews from Firebase and organize them by date
        reviewManager.getReviews { reviews ->
            activity?.runOnUiThread {
                val diaryEntries = organizeReviewsByDate(reviews)
                diaryAdapter.submitList(diaryEntries)
            }
        }
    }

    private fun organizeReviewsByDate(reviews: List<Review>): List<DiaryEntry> {
        val diaryEntries = mutableListOf<DiaryEntry>()

        // Parse dates from review timestamps and organize by day
        // This is a simplified version - you might need more complex date parsing
        val calendar = java.util.Calendar.getInstance()

        reviews.forEach { review ->
            try {
                // Parse the timestamp to extract day, month, year
                // This depends on your timestamp format "EEEE, dd MMMM yyyy"
                val parts = review.timestamp.split(" ")
                if (parts.size >= 4) {
                    val day = parts[1].toIntOrNull() ?: 1
                    val month = when (parts[2].uppercase()) {
                        "JANUARY" -> 1
                        "FEBRUARY" -> 2
                        "MARCH" -> 3
                        "APRIL" -> 4
                        "MAY" -> 5
                        "JUNE" -> 6
                        "JULY" -> 7
                        "AUGUST" -> 8
                        "SEPTEMBER" -> 9
                        "OCTOBER" -> 10
                        "NOVEMBER" -> 11
                        "DECEMBER" -> 12
                        else -> calendar.get(java.util.Calendar.MONTH) + 1
                    }
                    val year = parts[3].toIntOrNull() ?: calendar.get(java.util.Calendar.YEAR)

                    val music = Music(
                        id = review.musicId,
                        title = review.musicTitle,
                        artist = "", // You might want to store artist in review
                        album = review.musicTitle,
                        releaseYear = review.musicYear.toIntOrNull() ?: 0,
                        genre = "",
                        coverImage = review.musicCoverUrl ?: "",
                        averageRating = review.rating.toDouble(),
                        reviewCount = 1
                    )

                    diaryEntries.add(DiaryEntry(
                        music = music,
                        day = day,
                        month = month,
                        year = year,
                        userRating = review.rating,
                        hasReview = review.content.isNotEmpty(),
                        isLiked = review.liked,
                        reviewId = review.id,
                        reviewContent = review.content
                    ))
                }
            } catch (e: Exception) {
                // Handle date parsing errors
                e.printStackTrace()
            }
        }

        // Sort by date (newest first)
        return diaryEntries.sortedWith(compareBy(
            { it.year },
            { it.month },
            { it.day }
        )).reversed()
    }
}

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