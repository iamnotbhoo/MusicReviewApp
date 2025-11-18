package student.projects.musicreviewapp.auth

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import student.projects.musicreviewapp.models.Music
import student.projects.musicreviewapp.models.Review
import student.projects.musicreviewapp.models.UserList
import student.projects.musicreviewapp.models.FirestoreUser
import student.projects.musicreviewapp.repositories.FirebaseRepository

class FirebaseDataManager(private val context: Context) {

    private val authManager = AuthManager()
    private val repository = FirebaseRepository()

    // Use direct local managers for fallback, NOT Firebase managers
    private val localReviewManager = ReviewManager(context)
    private val localFavoriteManager = FavoriteAlbumsManager(context)
    private val localListManager = ListManager(context)
    private val localLikeManager = LikeManager(context)
    private val localPlaylistManager = PlaylistManager(context)

    private var useFirebase = true // Flag to toggle between local and Firebase

    // ========== REVIEW OPERATIONS ==========

    fun saveReview(review: Review, onComplete: (Boolean) -> Unit = {}) {
        Log.d("FirebaseDataManager", "💾 Saving review: ${review.id}")

        if (useFirebase && authManager.getCurrentUid() != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val success = repository.saveReview(review)
                    if (success) {
                        // Also save locally for offline access
                        localReviewManager.saveReview(review)
                        withContext(Dispatchers.Main) {
                            Log.d("FirebaseDataManager", "✅ Review saved successfully")
                            onComplete(true)
                        }
                    } else {
                        throw Exception("Failed to save review to Firebase")
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseDataManager", "❌ Firebase review save failed: ${e.message}")
                    // Fallback to local storage
                    localReviewManager.saveReview(review)
                    withContext(Dispatchers.Main) {
                        Log.d("FirebaseDataManager", "✅ Review saved locally (fallback)")
                        onComplete(true)
                    }
                }
            }
        } else {
            localReviewManager.saveReview(review)
            Log.d("FirebaseDataManager", "✅ Review saved locally only")
            onComplete(true)
        }
    }

    fun getReviews(onResult: (List<Review>) -> Unit) {
        Log.d("FirebaseDataManager", "🔄 Getting all reviews")

        if (useFirebase && authManager.getCurrentUid() != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val firebaseReviews = repository.getAllReviews()
                    withContext(Dispatchers.Main) {
                        Log.d(
                            "FirebaseDataManager",
                            "✅ Retrieved ${firebaseReviews.size} reviews from Firebase"
                        )
                        onResult(firebaseReviews)
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseDataManager", "❌ Firebase reviews fetch failed: ${e.message}")
                    // Fallback to local storage
                    val localReviews = localReviewManager.getReviews()
                    withContext(Dispatchers.Main) {
                        Log.d(
                            "FirebaseDataManager",
                            "✅ Retrieved ${localReviews.size} reviews locally (fallback)"
                        )
                        onResult(localReviews)
                    }
                }
            }
        } else {
            val localReviews = localReviewManager.getReviews()
            Log.d("FirebaseDataManager", "✅ Retrieved ${localReviews.size} reviews locally only")
            onResult(localReviews)
        }
    }

    fun getReviewsByAlbum(albumId: String, onResult: (List<Review>) -> Unit) {
        Log.d("FirebaseDataManager", "🔄 Getting reviews for album: $albumId")

        if (useFirebase && authManager.getCurrentUid() != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val reviews = repository.getReviewsByAlbum(albumId)
                    withContext(Dispatchers.Main) {
                        Log.d(
                            "FirebaseDataManager",
                            "✅ Retrieved ${reviews.size} reviews for album from Firebase"
                        )
                        onResult(reviews)
                    }
                } catch (e: Exception) {
                    Log.e(
                        "FirebaseDataManager",
                        "❌ Firebase album reviews fetch failed: ${e.message}"
                    )
                    // Fallback: filter local reviews by album
                    val localReviews = localReviewManager.getReviews()
                    val filteredReviews = localReviews.filter { it.musicId == albumId }
                    withContext(Dispatchers.Main) {
                        Log.d(
                            "FirebaseDataManager",
                            "✅ Retrieved ${filteredReviews.size} reviews for album locally (fallback)"
                        )
                        onResult(filteredReviews)
                    }
                }
            }
        } else {
            val localReviews = localReviewManager.getReviews()
            val filteredReviews = localReviews.filter { it.musicId == albumId }
            Log.d(
                "FirebaseDataManager",
                "✅ Retrieved ${filteredReviews.size} reviews for album locally only"
            )
            onResult(filteredReviews)
        }
    }

    fun getPopularReviews(onResult: (List<Review>) -> Unit) {
        Log.d("FirebaseDataManager", "🔄 Getting popular reviews")

        if (useFirebase && authManager.getCurrentUid() != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val reviews = repository.getPopularReviews()
                    withContext(Dispatchers.Main) {
                        Log.d(
                            "FirebaseDataManager",
                            "✅ Retrieved ${reviews.size} popular reviews from Firebase"
                        )
                        onResult(reviews)
                    }
                } catch (e: Exception) {
                    Log.e(
                        "FirebaseDataManager",
                        "❌ Firebase popular reviews fetch failed: ${e.message}"
                    )
                    val localReviews = localReviewManager.getReviews()
                    val sortedReviews = localReviews.sortedByDescending { it.likes }
                    withContext(Dispatchers.Main) {
                        Log.d(
                            "FirebaseDataManager",
                            "✅ Retrieved ${sortedReviews.size} popular reviews locally (fallback)"
                        )
                        onResult(sortedReviews)
                    }
                }
            }
        } else {
            val localReviews = localReviewManager.getReviews()
            val sortedReviews = localReviews.sortedByDescending { it.likes }
            Log.d(
                "FirebaseDataManager",
                "✅ Retrieved ${sortedReviews.size} popular reviews locally only"
            )
            onResult(sortedReviews)
        }
    }

    // ========== FAVORITE ALBUMS OPERATIONS ==========

    fun updateFavoriteAlbums(albums: List<Music>, onComplete: (Boolean) -> Unit = {}) {
        Log.d("FirebaseDataManager", "🔄 updateFavoriteAlbums called with ${albums.size} albums")

        if (useFirebase && authManager.getCurrentUid() != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userId = authManager.getCurrentUid()!!
                    Log.d(
                        "FirebaseDataManager",
                        "📝 Saving ${albums.size} FULL albums to Firebase for user: $userId"
                    )

                    // Use the NEW method that takes full Music objects
                    val success = repository.updateFavoriteAlbumsFull(userId, albums)
                    if (success) {
                        // Also save locally for offline access
                        Log.d("FirebaseDataManager", "💾 Saving ${albums.size} albums locally")
                        localFavoriteManager.saveFavoriteAlbums(albums)

                        withContext(Dispatchers.Main) {
                            Log.d(
                                "FirebaseDataManager",
                                "✅ Successfully updated ${albums.size} albums"
                            )
                            onComplete(true)
                        }
                    } else {
                        throw Exception("Failed to update favorite albums in Firebase")
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseDataManager", "❌ Firebase update failed: ${e.message}", e)
                    // Fallback to local only
                    localFavoriteManager.saveFavoriteAlbums(albums)
                    withContext(Dispatchers.Main) {
                        Log.d(
                            "FirebaseDataManager",
                            "✅ Saved ${albums.size} albums locally (fallback)"
                        )
                        onComplete(true)
                    }
                }
            }
        } else {
            // Use local storage only
            Log.d("FirebaseDataManager", "💾 Saving ${albums.size} albums locally only")
            localFavoriteManager.saveFavoriteAlbums(albums)
            onComplete(true)
        }
    }

    fun getFavoriteAlbums(onResult: (List<Music>) -> Unit) {
        Log.d("FirebaseDataManager", "🔄 getFavoriteAlbums called")

        if (useFirebase && authManager.getCurrentUid() != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userId = authManager.getCurrentUid()!!
                    Log.d(
                        "FirebaseDataManager",
                        "📖 Fetching FULL albums from Firebase for user: $userId"
                    )

                    val favoriteAlbums = repository.getFavoriteAlbumsWithDetails(userId)
                    Log.d(
                        "FirebaseDataManager",
                        "📖 Retrieved ${favoriteAlbums.size} albums from Firebase"
                    )

                    // Also update local storage with fresh data
                    if (favoriteAlbums.isNotEmpty()) {
                        Log.d(
                            "FirebaseDataManager",
                            "💾 Updating local storage with ${favoriteAlbums.size} albums from Firebase"
                        )
                        localFavoriteManager.saveFavoriteAlbums(favoriteAlbums)
                    }

                    withContext(Dispatchers.Main) {
                        Log.d(
                            "FirebaseDataManager",
                            "✅ Returning ${favoriteAlbums.size} albums from Firebase"
                        )
                        onResult(favoriteAlbums)
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseDataManager", "❌ Firebase fetch failed: ${e.message}", e)
                    // Fallback to local storage
                    val localFavorites = localFavoriteManager.getFavoriteAlbums()
                    Log.d(
                        "FirebaseDataManager",
                        "📖 Retrieved ${localFavorites.size} albums from local storage (fallback)"
                    )
                    withContext(Dispatchers.Main) {
                        onResult(localFavorites)
                    }
                }
            }
        } else {
            // Use local storage only
            val localFavorites = localFavoriteManager.getFavoriteAlbums()
            Log.d(
                "FirebaseDataManager",
                "📖 Retrieved ${localFavorites.size} albums from local storage only"
            )
            onResult(localFavorites)
        }
    }

    fun addFavoriteAlbum(album: Music, onComplete: (Boolean) -> Unit = {}) {
        Log.d("FirebaseDataManager", "⭐ Adding favorite album: ${album.title}")

        getFavoriteAlbums { currentFavorites ->
            val updatedFavorites = currentFavorites.toMutableList()

            // Check if we can add more albums (max 4) and if album doesn't already exist
            if (updatedFavorites.size < 4 && !updatedFavorites.any { it.id == album.id }) {
                updatedFavorites.add(album)
                updateFavoriteAlbums(updatedFavorites, onComplete)
            } else {
                Log.w("FirebaseDataManager", "⚠️ Cannot add album - max reached or already exists")
                onComplete(false) // Can't add more or already exists
            }
        }
    }

    // ========== LIST OPERATIONS ==========

    fun createList(userList: UserList, onComplete: (Boolean) -> Unit = {}) {
        Log.d("FirebaseDataManager", "🔄 Creating list: ${userList.name}")

        if (useFirebase && authManager.getCurrentUid() != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // First ensure user document exists
                    ensureUserDocumentExists(userList.creator)

                    val success = repository.saveList(userList)
                    if (success) {
                        // Also save locally
                        localListManager.createList(userList)
                        withContext(Dispatchers.Main) {
                            Log.d("FirebaseDataManager", "✅ List created successfully")
                            onComplete(true)
                        }
                    } else {
                        throw Exception("Failed to save list to Firebase")
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseDataManager", "❌ Firebase list creation failed: ${e.message}")
                    // Fallback to local
                    localListManager.createList(userList)
                    withContext(Dispatchers.Main) {
                        Log.d("FirebaseDataManager", "✅ List created locally (fallback)")
                        onComplete(true)
                    }
                }
            }
        } else {
            localListManager.createList(userList)
            Log.d("FirebaseDataManager", "✅ List created locally only")
            onComplete(true)
        }
    }

    fun getLists(onResult: (List<UserList>) -> Unit) {
        Log.d("FirebaseDataManager", "🔄 Getting user lists")

        if (useFirebase && authManager.getCurrentUid() != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userId = authManager.getCurrentUid()!!
                    val lists = repository.getUserLists(userId)
                    withContext(Dispatchers.Main) {
                        Log.d(
                            "FirebaseDataManager",
                            "✅ Retrieved ${lists.size} lists from Firebase"
                        )
                        onResult(lists)
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseDataManager", "❌ Firebase lists fetch failed: ${e.message}")
                    // Fallback to local
                    val localLists = localListManager.getLists()
                    withContext(Dispatchers.Main) {
                        Log.d(
                            "FirebaseDataManager",
                            "✅ Retrieved ${localLists.size} lists locally (fallback)"
                        )
                        onResult(localLists)
                    }
                }
            }
        } else {
            val localLists = localListManager.getLists()
            Log.d("FirebaseDataManager", "✅ Retrieved ${localLists.size} lists locally only")
            onResult(localLists)
        }
    }

    fun getAllPublicLists(onResult: (List<UserList>) -> Unit) {
        Log.d("FirebaseDataManager", "🔄 Getting all public lists")

        if (useFirebase && authManager.getCurrentUid() != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val lists = repository.getAllPublicLists()
                    withContext(Dispatchers.Main) {
                        Log.d(
                            "FirebaseDataManager",
                            "✅ Retrieved ${lists.size} public lists from Firebase"
                        )
                        onResult(lists)
                    }
                } catch (e: Exception) {
                    Log.e(
                        "FirebaseDataManager",
                        "❌ Firebase public lists fetch failed: ${e.message}"
                    )
                    // Fallback to local public lists
                    val localLists = localListManager.getLists()
                    val publicLists = localLists.filter { it.isPublic }
                    withContext(Dispatchers.Main) {
                        Log.d(
                            "FirebaseDataManager",
                            "✅ Retrieved ${publicLists.size} public lists locally (fallback)"
                        )
                        onResult(publicLists)
                    }
                }
            }
        } else {
            val localLists = localListManager.getLists()
            val publicLists = localLists.filter { it.isPublic }
            Log.d(
                "FirebaseDataManager",
                "✅ Retrieved ${publicLists.size} public lists locally only"
            )
            onResult(publicLists)
        }
    }

    // ========== LIKE OPERATIONS ==========

    fun likeAlbum(album: Music, onComplete: (Boolean) -> Unit = {}) {
        Log.d("FirebaseDataManager", "❤️ Liking album: ${album.title}")

        if (useFirebase && authManager.getCurrentUid() != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userId = authManager.getCurrentUid()!!
                    val success = repository.likeAlbum(userId, album.id)
                    if (success) {
                        // Also save locally
                        localLikeManager.likeAlbum(album)
                        withContext(Dispatchers.Main) {
                            Log.d("FirebaseDataManager", "✅ Album liked successfully")
                            onComplete(true)
                        }
                    } else {
                        throw Exception("Failed to like album in Firebase")
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseDataManager", "❌ Firebase like failed: ${e.message}")
                    localLikeManager.likeAlbum(album)
                    withContext(Dispatchers.Main) {
                        Log.d("FirebaseDataManager", "✅ Album liked locally (fallback)")
                        onComplete(true)
                    }
                }
            }
        } else {
            localLikeManager.likeAlbum(album)
            Log.d("FirebaseDataManager", "✅ Album liked locally only")
            onComplete(true)
        }
    }

    fun unlikeAlbum(album: Music, onComplete: (Boolean) -> Unit = {}) {
        Log.d("FirebaseDataManager", "💔 Unliking album: ${album.title}")

        if (useFirebase && authManager.getCurrentUid() != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userId = authManager.getCurrentUid()!!
                    val success = repository.unlikeAlbum(userId, album.id)
                    if (success) {
                        // Also remove locally
                        localLikeManager.unlikeAlbum(album.id)
                        withContext(Dispatchers.Main) {
                            Log.d("FirebaseDataManager", "✅ Album unliked successfully")
                            onComplete(true)
                        }
                    } else {
                        throw Exception("Failed to unlike album in Firebase")
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseDataManager", "❌ Firebase unlike failed: ${e.message}")
                    localLikeManager.unlikeAlbum(album.id)
                    withContext(Dispatchers.Main) {
                        Log.d("FirebaseDataManager", "✅ Album unliked locally (fallback)")
                        onComplete(true)
                    }
                }
            }
        } else {
            localLikeManager.unlikeAlbum(album.id)
            Log.d("FirebaseDataManager", "✅ Album unliked locally only")
            onComplete(true)
        }
    }

    fun isAlbumLiked(albumId: String, onResult: (Boolean) -> Unit) {
        Log.d("FirebaseDataManager", "🔍 Checking if album is liked: $albumId")

        if (useFirebase && authManager.getCurrentUid() != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userId = authManager.getCurrentUid()!!
                    val isLiked = repository.isAlbumLiked(userId, albumId)
                    withContext(Dispatchers.Main) {
                        Log.d("FirebaseDataManager", "✅ Album like status: $isLiked")
                        onResult(isLiked)
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseDataManager", "❌ Firebase like check failed: ${e.message}")
                    // Fallback to local check
                    val isLikedLocally = localLikeManager.isAlbumLiked(albumId)
                    withContext(Dispatchers.Main) {
                        Log.d(
                            "FirebaseDataManager",
                            "✅ Album like status locally: $isLikedLocally (fallback)"
                        )
                        onResult(isLikedLocally)
                    }
                }
            }
        } else {
            val isLikedLocally = localLikeManager.isAlbumLiked(albumId)
            Log.d(
                "FirebaseDataManager",
                "✅ Album like status locally: $isLikedLocally (local only)"
            )
            onResult(isLikedLocally)
        }
    }

    // ========== PLAYLIST OPERATIONS ==========

    fun addToPlaylist(music: Music, onComplete: (Boolean) -> Unit = {}) {
        Log.d("FirebaseDataManager", "🎵 Adding to playlist: ${music.title}")

        if (useFirebase && authManager.getCurrentUid() != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userId = authManager.getCurrentUid()!!
                    val success = repository.addToPlaylist(userId, music)
                    if (success) {
                        // Also save locally
                        localPlaylistManager.addToPlaylist(music)
                        withContext(Dispatchers.Main) {
                            Log.d("FirebaseDataManager", "✅ Added to playlist successfully")
                            onComplete(true)
                        }
                    } else {
                        throw Exception("Failed to add to playlist in Firebase")
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseDataManager", "❌ Firebase playlist add failed: ${e.message}")
                    localPlaylistManager.addToPlaylist(music)
                    withContext(Dispatchers.Main) {
                        Log.d("FirebaseDataManager", "✅ Added to playlist locally (fallback)")
                        onComplete(true)
                    }
                }
            }
        } else {
            localPlaylistManager.addToPlaylist(music)
            Log.d("FirebaseDataManager", "✅ Added to playlist locally only")
            onComplete(true)
        }
    }

    fun removeFromPlaylist(music: Music, onComplete: (Boolean) -> Unit = {}) {
        Log.d("FirebaseDataManager", "🗑️ Removing from playlist: ${music.title}")

        if (useFirebase && authManager.getCurrentUid() != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userId = authManager.getCurrentUid()!!
                    val success = repository.removeFromPlaylist(userId, music.id)
                    if (success) {
                        // Also remove locally
                        localPlaylistManager.removeFromPlaylist(music.id)
                        withContext(Dispatchers.Main) {
                            Log.d("FirebaseDataManager", "✅ Removed from playlist successfully")
                            onComplete(true)
                        }
                    } else {
                        throw Exception("Failed to remove from playlist in Firebase")
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseDataManager", "❌ Firebase playlist remove failed: ${e.message}")
                    localPlaylistManager.removeFromPlaylist(music.id)
                    withContext(Dispatchers.Main) {
                        Log.d("FirebaseDataManager", "✅ Removed from playlist locally (fallback)")
                        onComplete(true)
                    }
                }
            }
        } else {
            localPlaylistManager.removeFromPlaylist(music.id)
            Log.d("FirebaseDataManager", "✅ Removed from playlist locally only")
            onComplete(true)
        }
    }

    fun getPlaylist(onResult: (List<Music>) -> Unit) {
        Log.d("FirebaseDataManager", "🔄 Getting playlist")

        if (useFirebase && authManager.getCurrentUid() != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userId = authManager.getCurrentUid()!!
                    val playlist = repository.getPlaylist(userId)
                    withContext(Dispatchers.Main) {
                        Log.d(
                            "FirebaseDataManager",
                            "✅ Retrieved ${playlist.size} playlist items from Firebase"
                        )
                        onResult(playlist)
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseDataManager", "❌ Firebase playlist fetch failed: ${e.message}")
                    val localPlaylist = localPlaylistManager.getPlaylist()
                    withContext(Dispatchers.Main) {
                        Log.d(
                            "FirebaseDataManager",
                            "✅ Retrieved ${localPlaylist.size} playlist items locally (fallback)"
                        )
                        onResult(localPlaylist)
                    }
                }
            }
        } else {
            val localPlaylist = localPlaylistManager.getPlaylist()
            Log.d(
                "FirebaseDataManager",
                "✅ Retrieved ${localPlaylist.size} playlist items locally only"
            )
            onResult(localPlaylist)
        }
    }

    // ========== UTILITY METHODS ==========

    fun getReviewCount(onResult: (Int) -> Unit) {
        getReviews { reviews ->
            onResult(reviews.size)
        }
    }

    fun hasUserReviewedAlbum(albumId: String, onResult: (Boolean) -> Unit) {
        val userId = authManager.getCurrentUid()
        if (userId != null) {
            getReviewsByAlbum(albumId) { reviews ->
                val hasReviewed = reviews.any { it.userId == userId }
                Log.d("FirebaseDataManager", "🔍 User has reviewed album $albumId: $hasReviewed")
                onResult(hasReviewed)
            }
        } else {
            Log.d("FirebaseDataManager", "🔍 User not logged in, cannot check review status")
            onResult(false)
        }
    }

    fun getUserReviews(userId: String, onResult: (List<Review>) -> Unit) {
        getReviews { reviews ->
            val userReviews = reviews.filter { it.userId == userId }
            Log.d(
                "FirebaseDataManager",
                "📖 Retrieved ${userReviews.size} reviews for user: $userId"
            )
            onResult(userReviews)
        }
    }

    fun getRecentReviews(limit: Int = 10, onResult: (List<Review>) -> Unit) {
        getReviews { reviews ->
            val recentReviews = reviews.take(limit)
            Log.d("FirebaseDataManager", "📖 Retrieved ${recentReviews.size} recent reviews")
            onResult(recentReviews)
        }
    }

    // ========== MIGRATION & SETUP ==========

    fun migrateLocalDataToFirebase(onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        Log.d("FirebaseDataManager", "🔄 Starting local data migration to Firebase")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = authManager.getCurrentUid() ?: throw Exception("User not logged in")

                // Ensure user document exists first
                ensureUserDocumentExists(userId)

                // Migrate reviews
                val localReviews = localReviewManager.getReviews()
                localReviews.forEach { review ->
                    val updatedReview = review.copy(userId = userId)
                    repository.saveReview(updatedReview)
                }
                Log.d("Migration", "✅ Migrated ${localReviews.size} reviews")

                // Migrate favorite albums - USE NEW FORMAT
                val favoriteAlbums = localFavoriteManager.getFavoriteAlbums()
                if (favoriteAlbums.isNotEmpty()) {
                    // Use the NEW method that takes full Music objects
                    repository.updateFavoriteAlbumsFull(userId, favoriteAlbums)
                    Log.d("Migration", "✅ Migrated ${favoriteAlbums.size} albums to new format")
                }

                // Continue with other migrations...
                migrateListsAndPlaylist(userId, onComplete)

            } catch (e: Exception) {
                Log.e("Migration", "❌ Migration failed: ${e.message}")
                withContext(Dispatchers.Main) {
                    onComplete(false, "Migration failed: ${e.message}")
                }
            }
        }
    }

    private suspend fun migrateListsAndPlaylist(
        userId: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        try {
            // Migrate lists
            val localLists = localListManager.getLists()
            localLists.forEach { list ->
                repository.saveList(list)
            }
            Log.d("Migration", "✅ Migrated ${localLists.size} lists")

            // Migrate playlist
            val localPlaylist = localPlaylistManager.getPlaylist()
            localPlaylist.forEach { music ->
                repository.addToPlaylist(userId, music)
            }
            Log.d("Migration", "✅ Migrated ${localPlaylist.size} playlist items")

            withContext(Dispatchers.Main) {
                onComplete(true, "Data migrated successfully!")
            }
        } catch (e: Exception) {
            Log.e("Migration", "❌ Lists/Playlist migration failed: ${e.message}")
            withContext(Dispatchers.Main) {
                onComplete(false, "Migration failed: ${e.message}")
            }
        }
    }

    private suspend fun ensureUserDocumentExists(userId: String) {
        try {
            val existingUser = repository.getUser(userId)
            if (existingUser == null) {
                // Create basic user document
                val newUser = FirestoreUser(
                    uid = userId,
                    username = "User", // Default username
                    email = "", // Will be updated when available
                    reviewedAlbums = emptyList(),
                    favoriteAlbums = emptyList(),
                    likedAlbums = emptyList(),
                    likedReviews = emptyList(),
                    likedLists = emptyList(),
                    createdLists = emptyList()
                )
                repository.createUser(newUser)
                Log.d("FirebaseDataManager", "✅ Created user document for: $userId")
            }
        } catch (e: Exception) {
            Log.e("FirebaseDataManager", "❌ Error ensuring user document exists: ${e.message}")
        }
    }

    // ========== SETTINGS ==========

    fun setUseFirebase(useFirebase: Boolean) {
        this.useFirebase = useFirebase
        Log.d("FirebaseDataManager", "⚙️ Firebase usage set to: $useFirebase")
    }

    fun isUsingFirebase(): Boolean {
        return useFirebase && authManager.getCurrentUid() != null
    }

    fun getCurrentUserId(): String? {
        return authManager.getCurrentUid()
    }

    // ========== DEBUG METHODS ==========

    fun debugCheckDataFormat() {
        if (useFirebase && authManager.getCurrentUid() != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userId = authManager.getCurrentUid()!!

                    // Just check by retrieving favorites - this will show which format is being used
                    val favoriteAlbums = repository.getFavoriteAlbumsWithDetails(userId)
                    val favoriteAlbumIds = repository.getFavoriteAlbums(userId)

                    Log.d(
                        "DataFormat",
                        "📊 User $userId - Full albums: ${favoriteAlbums.size}, Album IDs: ${favoriteAlbumIds.size}"
                    )

                    // Check if we're using new format by seeing if full albums match the expected data
                    val hasFullData =
                        favoriteAlbums.isNotEmpty() && favoriteAlbums.all { it.title.isNotEmpty() }
                    Log.d("DataFormat", "📊 Using new format with full data: $hasFullData")

                } catch (e: Exception) {
                    Log.e("DataFormat", "❌ Check failed: ${e.message}")
                }
            }
        }
    }
}