// utils/DataMigrator.kt
package student.projects.musicreviewapp.utils

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import student.projects.musicreviewapp.auth.AuthManager
import student.projects.musicreviewapp.auth.FirebaseFavoriteAlbumsManager
import student.projects.musicreviewapp.auth.ReviewManager
import student.projects.musicreviewapp.repositories.FirebaseRepository

class DataMigrator(private val context: Context) {

    private val authManager = AuthManager()
    private val localReviewManager = ReviewManager(context)
    private val localFavoriteManager = FirebaseFavoriteAlbumsManager(context)
    private val firebaseRepository = FirebaseRepository()

    // Helper function to convert callback to suspend function
    private suspend fun getFavoriteAlbumsSuspend(): List<student.projects.musicreviewapp.models.Music> =
        suspendCancellableCoroutine { continuation ->
            localFavoriteManager.getFavoriteAlbums { albums ->
                continuation.resume(albums)
            }
        }

    fun migrateUserData(onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        val userId = authManager.getCurrentUid() ?: run {
            onComplete(false, "User not logged in")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Migrate reviews
                val localReviews = localReviewManager.getReviews()
                localReviews.forEach { review ->
                    val updatedReview = review.copy(userId = userId)
                    firebaseRepository.saveReview(updatedReview)
                }

                // Migrate favorite albums using suspend function
                val favoriteAlbums = getFavoriteAlbumsSuspend()
                val albumIds = favoriteAlbums.map { it.id }

                if (albumIds.isNotEmpty()) {
                    // Save album data first
                    favoriteAlbums.forEach { album ->
                        firebaseRepository.saveAlbum(album)
                    }
                    firebaseRepository.updateFavoriteAlbums(userId, albumIds)
                }

                // Clear local data after successful migration (optional)
                // You might want to clear SharedPreferences here

                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true, "Data migrated successfully: ${localReviews.size} reviews, ${albumIds.size} favorites")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(false, "Migration failed: ${e.message}")
                }
            }
        }
    }
}