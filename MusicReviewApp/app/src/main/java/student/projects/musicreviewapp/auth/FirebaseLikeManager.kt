package student.projects.musicreviewapp.auth

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import student.projects.musicreviewapp.models.Music
import student.projects.musicreviewapp.models.Review
import student.projects.musicreviewapp.models.UserList
import student.projects.musicreviewapp.repositories.FirebaseRepository

class FirebaseLikeManager(private val context: Context) {

    private val repository = FirebaseRepository()
    private val authManager = AuthManager()
    private val localLikeManager = LikeManager(context)

    // Album Likes
    fun likeAlbum(album: Music, onComplete: (Boolean) -> Unit = {}) {
        val userId = authManager.getCurrentUid() ?: return

        // Save locally first for immediate UI update
        localLikeManager.likeAlbum(album)

        // Then sync with Firebase
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.likeAlbum(userId, album.id)
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true)
                }
            } catch (e: Exception) {
                // Keep local data if Firebase fails
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true) // Still true because local save worked
                }
            }
        }
    }

    fun unlikeAlbum(albumId: String, onComplete: (Boolean) -> Unit = {}) {
        val userId = authManager.getCurrentUid() ?: return

        // Remove locally first
        localLikeManager.unlikeAlbum(albumId)

        // Then sync with Firebase
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.unlikeAlbum(userId, albumId)
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true)
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true) // Still true because local removal worked
                }
            }
        }
    }

    fun getLikedAlbums(onResult: (List<String>) -> Unit) {
        val userId = authManager.getCurrentUid() ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val firebaseLikedAlbums = repository.getLikedAlbums(userId)
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(firebaseLikedAlbums)
                }
            } catch (e: Exception) {
                // Fallback to local storage
                val localLikedAlbums = localLikeManager.getLikedAlbums().map { it.id }
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(localLikedAlbums)
                }
            }
        }
    }

    fun isAlbumLiked(albumId: String, onResult: (Boolean) -> Unit) {
        val userId = authManager.getCurrentUid() ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val isLiked = repository.isAlbumLiked(userId, albumId)
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(isLiked)
                }
            } catch (e: Exception) {
                // Fallback to local storage
                val isLiked = localLikeManager.isAlbumLiked(albumId)
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(isLiked)
                }
            }
        }
    }

    // Review Likes
    fun likeReview(review: Review, onComplete: (Boolean) -> Unit = {}) {
        val userId = authManager.getCurrentUid() ?: return

        // Save locally first
        localLikeManager.likeReview(review)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.likeReview(userId, review.id)
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true)
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true) // Local save worked
                }
            }
        }
    }

    fun unlikeReview(reviewId: String, onComplete: (Boolean) -> Unit = {}) {
        val userId = authManager.getCurrentUid() ?: return

        // Remove locally first
        localLikeManager.unlikeReview(reviewId)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.unlikeReview(userId, reviewId)
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true)
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true) // Local removal worked
                }
            }
        }
    }

    // ADD THIS MISSING METHOD
    fun getLikedReviews(onResult: (List<String>) -> Unit) {
        val userId = authManager.getCurrentUid() ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val firebaseLikedReviews = repository.getLikedReviews(userId)
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(firebaseLikedReviews)
                }
            } catch (e: Exception) {
                // Fallback to local storage
                val localLikedReviews = localLikeManager.getLikedReviews().map { it.id }
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(localLikedReviews)
                }
            }
        }
    }

    fun isReviewLiked(reviewId: String, onResult: (Boolean) -> Unit) {
        val userId = authManager.getCurrentUid() ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val isLiked = repository.isReviewLiked(userId, reviewId)
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(isLiked)
                }
            } catch (e: Exception) {
                // Fallback to local storage
                val isLiked = localLikeManager.isReviewLiked(reviewId)
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(isLiked)
                }
            }
        }
    }

    // List Likes
    fun likeList(list: UserList, onComplete: (Boolean) -> Unit = {}) {
        val userId = authManager.getCurrentUid() ?: return

        // Save locally first
        localLikeManager.likeList(list)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.likeList(userId, list.id)
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true)
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true) // Local save worked
                }
            }
        }
    }

    fun unlikeList(listId: String, onComplete: (Boolean) -> Unit = {}) {
        val userId = authManager.getCurrentUid() ?: return

        // Remove locally first
        localLikeManager.unlikeList(listId)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.unlikeList(userId, listId)
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true)
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true) // Local removal worked
                }
            }
        }
    }

    // ADD THIS MISSING METHOD
    fun getLikedLists(onResult: (List<String>) -> Unit) {
        val userId = authManager.getCurrentUid() ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val firebaseLikedLists = repository.getLikedLists(userId)
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(firebaseLikedLists)
                }
            } catch (e: Exception) {
                // Fallback to local storage
                val localLikedLists = localLikeManager.getLikedLists().map { it.id }
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(localLikedLists)
                }
            }
        }
    }

    fun isListLiked(listId: String, onResult: (Boolean) -> Unit) {
        val userId = authManager.getCurrentUid() ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val isLiked = repository.isListLiked(userId, listId)
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(isLiked)
                }
            } catch (e: Exception) {
                // Fallback to local storage
                val isLiked = localLikeManager.isListLiked(listId)
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(isLiked)
                }
            }
        }
    }

    // Sync local likes to Firebase (for migration)
    fun syncLocalLikesToFirebase(onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        val userId = authManager.getCurrentUid() ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Sync liked albums
                val likedAlbums = localLikeManager.getLikedAlbums()
                likedAlbums.forEach { album ->
                    repository.likeAlbum(userId, album.id)
                }

                // Sync liked reviews
                val likedReviews = localLikeManager.getLikedReviews()
                likedReviews.forEach { review ->
                    repository.likeReview(userId, review.id)
                }

                // Sync liked lists
                val likedLists = localLikeManager.getLikedLists()
                likedLists.forEach { list ->
                    repository.likeList(userId, list.id)
                }

                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true, "Likes synced successfully")
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(false, "Failed to sync likes: ${e.message}")
                }
            }
        }
    }
}