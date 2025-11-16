package student.projects.musicreviewapp.auth

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import student.projects.musicreviewapp.models.Review
import java.text.SimpleDateFormat
import java.util.*

class ReviewManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_reviews", Context.MODE_PRIVATE)
    private val key = "user_reviews_list"

    fun saveReview(review: Review) {
        val currentReviews = getReviews().toMutableList()

        // Check if review already exists for this album
        val existingIndex = currentReviews.indexOfFirst { it.musicId == review.musicId }
        if (existingIndex != -1) {
            // Update existing review
            currentReviews[existingIndex] = review
        } else {
            // Add new review
            currentReviews.add(0, review) // Add to beginning for most recent first
        }

        saveReviewsToStorage(currentReviews)
    }

    fun getReviews(): List<Review> {
        val jsonString = sharedPreferences.getString(key, null)
        return if (jsonString != null) {
            parseReviewsFromJson(jsonString)
        } else {
            emptyList()
        }
    }

    fun getRecentReviews(limit: Int = 10): List<Review> {
        return getReviews().take(limit)
    }

    fun getReviewByAlbumId(albumId: String): Review? {
        return getReviews().find { it.musicId == albumId }
    }

    fun deleteReview(albumId: String) {
        val currentReviews = getReviews().toMutableList()
        currentReviews.removeAll { it.musicId == albumId }
        saveReviewsToStorage(currentReviews)
    }

    private fun saveReviewsToStorage(reviews: List<Review>) {
        val jsonString = convertReviewsToJson(reviews)
        sharedPreferences.edit().putString(key, jsonString).apply()
    }

    private fun convertReviewsToJson(reviews: List<Review>): String {
        val jsonArray = JSONArray()
        reviews.forEach { review ->
            val jsonObject = JSONObject().apply {
                put("id", review.id)
                put("userId", review.userId)
                put("userName", review.userName)
                put("userPhotoUrl", review.userPhotoUrl ?: "")
                put("content", review.content)
                put("timestamp", review.timestamp)
                put("musicId", review.musicId)
                put("musicTitle", review.musicTitle)
                put("musicYear", review.musicYear)
                put("musicCoverUrl", review.musicCoverUrl ?: "")
                put("rating", review.rating)
                put("tags", JSONArray(review.tags))
                put("isFirstListen", review.isFirstListen)
                put("allowReplies", review.allowReplies)
                put("liked", review.liked)
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    private fun parseReviewsFromJson(jsonString: String): List<Review> {
        val reviews = mutableListOf<Review>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val tagsArray = jsonObject.getJSONArray("tags")
                val tags = mutableListOf<String>()
                for (j in 0 until tagsArray.length()) {
                    tags.add(tagsArray.getString(j))
                }

                val review = Review(
                    id = jsonObject.getString("id"),
                    userId = jsonObject.getString("userId"),
                    userName = jsonObject.getString("userName"),
                    userPhotoUrl = jsonObject.optString("userPhotoUrl").takeIf { it.isNotEmpty() },
                    content = jsonObject.getString("content"),
                    timestamp = jsonObject.getString("timestamp"),
                    musicId = jsonObject.getString("musicId"),
                    musicTitle = jsonObject.getString("musicTitle"),
                    musicYear = jsonObject.getString("musicYear"),
                    musicCoverUrl = jsonObject.optString("musicCoverUrl").takeIf { it.isNotEmpty() },
                    rating = jsonObject.getInt("rating"),
                    tags = tags,
                    isFirstListen = jsonObject.getBoolean("isFirstListen"),
                    allowReplies = jsonObject.getBoolean("allowReplies"),
                    liked = jsonObject.getBoolean("liked")
                )
                reviews.add(review)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return reviews
    }

    fun generateReviewId(): String {
        return "review_${System.currentTimeMillis()}"
    }

    fun getCurrentTimestamp(): String {
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }
}