package student.projects.musicreviewapp.auth

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import student.projects.musicreviewapp.models.Review
import student.projects.musicreviewapp.repositories.FirebaseRepository
import java.text.SimpleDateFormat
import java.util.*

class FirebaseReviewManager(private val context: Context) {

    private val repository = FirebaseRepository()
    private val authManager = AuthManager()
    private val localManager = ReviewManager(context)

    fun saveReview(review: Review, onComplete: (Boolean) -> Unit = {}) {
        // Save locally first for immediate UI update
        localManager.saveReview(review)

        // Then sync with Firebase
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.saveReview(review)
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true)
                }
            } catch (e: Exception) {
                // Firebase failed, but local save worked
                e.printStackTrace()
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true)
                }
            }
        }
    }

    fun getReviews(onResult: (List<Review>) -> Unit) {
        val userId = authManager.getCurrentUid()

        if (userId != null) {
            // Try Firebase first
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val firebaseReviews = repository.getAllReviews()
                    CoroutineScope(Dispatchers.Main).launch {
                        onResult(firebaseReviews)
                    }
                } catch (e: Exception) {
                    // Fallback to local storage
                    val localReviews = localManager.getReviews()
                    CoroutineScope(Dispatchers.Main).launch {
                        onResult(localReviews)
                    }
                }
            }
        } else {
            // No user logged in, use local storage
            val localReviews = localManager.getReviews()
            onResult(localReviews)
        }
    }

    fun getRecentReviews(limit: Int = 10, onResult: (List<Review>) -> Unit) {
        getReviews { reviews ->
            onResult(reviews.take(limit))
        }
    }

    // Get reviews by user
    fun getReviewsByUser(userId: String, onResult: (List<Review>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reviews = repository.getReviewsByUser(userId)
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(reviews)
                }
            } catch (e: Exception) {
                // Fallback to local storage
                val localReviews = localManager.getReviews().filter { it.userId == userId }
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(localReviews)
                }
            }
        }
    }

    // Get reviews by album
    fun getReviewsByAlbum(albumId: String, onResult: (List<Review>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reviews = repository.getReviewsByAlbum(albumId)
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(reviews)
                }
            } catch (e: Exception) {
                // Fallback to local storage
                val localReviews = localManager.getReviews().filter { it.musicId == albumId }
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(localReviews)
                }
            }
        }
    }

    // Get popular reviews
    fun getPopularReviews(onResult: (List<Review>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reviews = repository.getPopularReviews()
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(reviews)
                }
            } catch (e: Exception) {
                // Fallback to local storage
                val localReviews = localManager.getReviews().sortedByDescending { it.likes }
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(localReviews)
                }
            }
        }
    }

    suspend fun likeReview(reviewId: String, userId: String): Boolean {
        return try {
            repository.likeReview(userId, reviewId)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun unlikeReview(reviewId: String, userId: String): Boolean {
        return try {
            repository.unlikeReview(userId, reviewId)
            true
        } catch (e: Exception) {
            false
        }
    }

    // Delete a review
    fun deleteReview(reviewId: String, userId: String, onComplete: (Boolean) -> Unit = {}) {
        // Delete locally first
        localManager.deleteReview(reviewId)

        // Then sync with Firebase
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.deleteReview(reviewId, userId)
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true)
                }
            } catch (e: Exception) {
                // Firebase failed, but local deletion worked
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true)
                }
            }
        }
    }

    // Additional utility methods matching your original ReviewManager
    fun getReviewByAlbumId(albumId: String, onResult: (Review?) -> Unit) {
        getReviews { reviews ->
            onResult(reviews.find { it.musicId == albumId })
        }
    }

    fun hasUserReviewedAlbum(albumId: String, onResult: (Boolean) -> Unit) {
        val userId = authManager.getCurrentUid()
        if (userId != null) {
            getReviewsByUser(userId) { reviews ->
                onResult(reviews.any { it.musicId == albumId })
            }
        } else {
            onResult(false)
        }
    }

    fun getReviewedAlbumIds(onResult: (List<String>) -> Unit) {
        getReviews { reviews ->
            onResult(reviews.map { it.musicId })
        }
    }

    fun getReviewCount(onResult: (Int) -> Unit) {
        getReviews { reviews ->
            onResult(reviews.size)
        }
    }

    fun generateReviewId(): String {
        return localManager.generateReviewId()
    }

    fun getCurrentTimestamp(): String {
        return localManager.getCurrentTimestamp()
    }

    // Sync local reviews to Firebase (for migration)
    fun syncLocalReviewsToFirebase(onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        val userId = authManager.getCurrentUid() ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val localReviews = localManager.getReviews()
                localReviews.forEach { review ->
                    // Update userId to current user if needed
                    val updatedReview = if (review.userId != userId) {
                        review.copy(userId = userId)
                    } else {
                        review
                    }
                    repository.saveReview(updatedReview)
                }

                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true, "Reviews synced successfully")
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(false, "Failed to sync reviews: ${e.message}")
                }
            }
        }
    }
}