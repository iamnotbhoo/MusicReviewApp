package student.projects.musicreviewapp.repositories

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import student.projects.musicreviewapp.models.FirestoreUser
import student.projects.musicreviewapp.models.Music
import student.projects.musicreviewapp.models.Review
import student.projects.musicreviewapp.models.UserList

class FirebaseRepository {
    private val db: FirebaseFirestore = Firebase.firestore

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val REVIEWS_COLLECTION = "reviews"
        private const val LISTS_COLLECTION = "lists"
        private const val LIKES_COLLECTION = "likes"
        private const val ALBUMS_COLLECTION = "albums"
    }

    // ========== USER OPERATIONS ==========

    suspend fun getUser(userId: String): FirestoreUser? {
        return try {
            val userDoc = db.collection(USERS_COLLECTION).document(userId).get().await()
            if (userDoc.exists()) {
                userDoc.toObject(FirestoreUser::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error getting user: ${e.message}")
            null
        }
    }

    suspend fun getUserWithFullData(userId: String): Map<String, Any> {
        return try {
            val userDoc = db.collection(USERS_COLLECTION).document(userId).get().await()
            if (userDoc.exists()) {
                val data = userDoc.data ?: emptyMap()

                // Add favorite albums in both formats for backward compatibility
                val favoriteAlbumsFull = data["favoriteAlbumsFull"] as? List<Map<String, Any>>
                val favoriteAlbums = data["favoriteAlbums"] as? List<String>

                val enhancedData = data.toMutableMap()
                if (favoriteAlbumsFull != null) {
                    enhancedData["favoriteAlbumsFull"] = favoriteAlbumsFull
                }
                if (favoriteAlbums != null) {
                    enhancedData["favoriteAlbums"] = favoriteAlbums
                }

                enhancedData
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error getting user with full data: ${e.message}")
            emptyMap()
        }
    }

    suspend fun createUser(user: FirestoreUser): Boolean {
        return try {
            Log.d("FirebaseRepository", "👤 Creating user: ${user.uid}")
            db.collection(USERS_COLLECTION).document(user.uid).set(user).await()
            Log.d("FirebaseRepository", "✅ User created successfully")
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error creating user: ${e.message}")
            false
        }
    }

    suspend fun updateUser(userId: String, updates: Map<String, Any>): Boolean {
        return try {
            Log.d("FirebaseRepository", "📝 Updating user: $userId")
            db.collection(USERS_COLLECTION).document(userId).update(updates).await()
            Log.d("FirebaseRepository", "✅ User updated successfully")
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error updating user: ${e.message}")
            false
        }
    }

    suspend fun getUserProfile(userId: String): Map<String, Any>? {
        return try {
            val userDoc = db.collection(USERS_COLLECTION).document(userId).get().await()
            if (userDoc.exists()) {
                userDoc.data
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error getting user profile: ${e.message}")
            null
        }
    }

    // ========== REVIEW OPERATIONS ==========

    suspend fun saveReview(review: Review): Boolean {
        return try {
            Log.d("FirebaseRepository", "💾 Saving review: ${review.id} for album: ${review.musicId}")

            // Save the review to reviews collection
            db.collection(REVIEWS_COLLECTION).document(review.id).set(review).await()
            Log.d("FirebaseRepository", "✅ Review saved to reviews collection")

            // Update user's reviewed albums with backward compatibility
            val userRef = db.collection(USERS_COLLECTION).document(review.userId)
            val userDoc = userRef.get().await()

            if (userDoc.exists()) {
                // Update existing user
                db.runTransaction { transaction ->
                    val existingUserDoc = transaction.get(userRef)
                    val currentReviewed = existingUserDoc.get("reviewedAlbums") as? List<String> ?: emptyList()
                    val updatedReviewed = (currentReviewed + review.musicId).distinct()
                    transaction.update(userRef, "reviewedAlbums", updatedReviewed)
                }.await()
                Log.d("FirebaseRepository", "✅ User reviewed albums updated")
            } else {
                // Create new user document with both old and new fields
                val newUser = FirestoreUser(
                    uid = review.userId,
                    username = review.userName,
                    email = "",
                    reviewedAlbums = listOf(review.musicId),
                    favoriteAlbums = emptyList(),
                    likedAlbums = emptyList(),
                    likedReviews = emptyList(),
                    likedLists = emptyList(),
                    createdLists = emptyList()
                )
                userRef.set(newUser).await()
                Log.d("FirebaseRepository", "✅ Created new user document with reviewed album")
            }

            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error saving review: ${e.message}")
            false
        }
    }

    suspend fun getAllReviews(): List<Review> {
        return try {
            val reviews = db.collection(REVIEWS_COLLECTION).get().await().toObjects(Review::class.java)
            Log.d("FirebaseRepository", "📖 Retrieved ${reviews.size} reviews")
            reviews
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error getting all reviews: ${e.message}")
            emptyList()
        }
    }

    suspend fun getReviewsByUser(userId: String): List<Review> {
        return try {
            val reviews = db.collection(REVIEWS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get().await()
                .toObjects(Review::class.java)
            Log.d("FirebaseRepository", "📖 Retrieved ${reviews.size} reviews for user: $userId")
            reviews
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error getting user reviews: ${e.message}")
            emptyList()
        }
    }

    suspend fun getReviewsByAlbum(albumId: String): List<Review> {
        return try {
            val reviews = db.collection(REVIEWS_COLLECTION)
                .whereEqualTo("musicId", albumId)
                .get().await()
                .toObjects(Review::class.java)
            Log.d("FirebaseRepository", "📖 Retrieved ${reviews.size} reviews for album: $albumId")
            reviews
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error getting album reviews: ${e.message}")
            emptyList()
        }
    }

    suspend fun getPopularReviews(): List<Review> {
        return try {
            val reviews = getAllReviews().sortedByDescending { it.likes }
            Log.d("FirebaseRepository", "📖 Retrieved ${reviews.size} popular reviews")
            reviews
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error getting popular reviews: ${e.message}")
            emptyList()
        }
    }

    suspend fun deleteReview(reviewId: String, userId: String): Boolean {
        return try {
            db.collection(REVIEWS_COLLECTION).document(reviewId).delete().await()
            Log.d("FirebaseRepository", "✅ Review deleted: $reviewId")
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error deleting review: ${e.message}")
            false
        }
    }

    // ========== LIST OPERATIONS ==========

    suspend fun saveList(userList: UserList): Boolean {
        return try {
            Log.d("FirebaseRepository", "💾 Saving list: ${userList.id}")

            // Save the list to lists collection
            db.collection(LISTS_COLLECTION).document(userList.id).set(userList).await()
            Log.d("FirebaseRepository", "✅ List saved to lists collection")

            // Add to user's created lists with backward compatibility
            val userRef = db.collection(USERS_COLLECTION).document(userList.creator)
            val userDoc = userRef.get().await()

            if (userDoc.exists()) {
                db.runTransaction { transaction ->
                    val existingUserDoc = transaction.get(userRef)
                    val currentLists = existingUserDoc.get("createdLists") as? List<String> ?: emptyList()
                    val updatedLists = (currentLists + userList.id).distinct()
                    transaction.update(userRef, "createdLists", updatedLists)
                }.await()
                Log.d("FirebaseRepository", "✅ Added list to user's created lists")
            } else {
                // Create user document if it doesn't exist
                val newUser = FirestoreUser(
                    uid = userList.creator,
                    username = "User",
                    email = "",
                    reviewedAlbums = emptyList(),
                    favoriteAlbums = emptyList(),
                    likedAlbums = emptyList(),
                    likedReviews = emptyList(),
                    likedLists = emptyList(),
                    createdLists = listOf(userList.id)
                )
                userRef.set(newUser).await()
                Log.d("FirebaseRepository", "✅ Created user document with created list")
            }

            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error saving list: ${e.message}")
            false
        }
    }

    suspend fun getUserLists(userId: String): List<UserList> {
        return try {
            val lists = db.collection(LISTS_COLLECTION)
                .whereEqualTo("creator", userId)
                .get().await()
                .toObjects(UserList::class.java)
            Log.d("FirebaseRepository", "📖 Retrieved ${lists.size} lists for user: $userId")

            // Debug: Log the actual query results
            lists.forEach { list ->
                Log.d("FirebaseRepository", "📋 List: ${list.name}, Creator: ${list.creator}, ID: ${list.id}")
            }

            lists
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error getting user lists: ${e.message}")
            emptyList()
        }
    }

    // Add this method to debug list storage
    suspend fun debugGetAllLists(): List<UserList> {
        return try {
            val lists = db.collection(LISTS_COLLECTION).get().await().toObjects(UserList::class.java)
            Log.d("FirebaseRepository", "🔍 DEBUG: Found ${lists.size} total lists in database")
            lists.forEach { list ->
                Log.d("FirebaseRepository", "🔍 List: ${list.name}, Creator: ${list.creator}, ID: ${list.id}, Public: ${list.isPublic}")
            }
            lists
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error debugging lists: ${e.message}")
            emptyList()
        }
    }

    suspend fun getAllPublicLists(): List<UserList> {
        return try {
            val lists = db.collection(LISTS_COLLECTION)
                .whereEqualTo("isPublic", true)
                .get().await()
                .toObjects(UserList::class.java)
            Log.d("FirebaseRepository", "📖 Retrieved ${lists.size} public lists")
            lists
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error getting public lists: ${e.message}")
            emptyList()
        }
    }

    suspend fun deleteList(listId: String, userId: String): Boolean {
        return try {
            db.collection(LISTS_COLLECTION).document(listId).delete().await()
            Log.d("FirebaseRepository", "✅ List deleted: $listId")
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error deleting list: ${e.message}")
            false
        }
    }

    // ========== LIKE OPERATIONS ==========

    suspend fun likeAlbum(userId: String, albumId: String): Boolean {
        return try {
            Log.d("FirebaseRepository", "❤️ User $userId liking album: $albumId")

            // Save like to likes collection
            val likeData = mapOf<String, Any>(
                "userId" to userId,
                "albumId" to albumId,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection(LIKES_COLLECTION).document("${userId}_album_$albumId").set(likeData).await()

            // Update user's liked albums with backward compatibility
            val userRef = db.collection(USERS_COLLECTION).document(userId)
            val userDoc = userRef.get().await()

            if (userDoc.exists()) {
                db.runTransaction { transaction ->
                    val existingUserDoc = transaction.get(userRef)
                    val currentLiked = existingUserDoc.get("likedAlbums") as? List<String> ?: emptyList()
                    val updatedLiked = (currentLiked + albumId).distinct()
                    transaction.update(userRef, "likedAlbums", updatedLiked)
                }.await()
            } else {
                // Create user document if it doesn't exist
                val newUser = FirestoreUser(
                    uid = userId,
                    username = "User",
                    email = "",
                    reviewedAlbums = emptyList(),
                    favoriteAlbums = emptyList(),
                    likedAlbums = listOf(albumId),
                    likedReviews = emptyList(),
                    likedLists = emptyList(),
                    createdLists = emptyList()
                )
                userRef.set(newUser).await()
            }

            Log.d("FirebaseRepository", "✅ Album liked successfully")
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error liking album: ${e.message}")
            false
        }
    }

    suspend fun unlikeAlbum(userId: String, albumId: String): Boolean {
        return try {
            Log.d("FirebaseRepository", "💔 User $userId unliking album: $albumId")

            // Remove like from likes collection
            db.collection(LIKES_COLLECTION).document("${userId}_album_$albumId").delete().await()

            // Update user's liked albums
            val userRef = db.collection(USERS_COLLECTION).document(userId)
            db.runTransaction { transaction ->
                val userDoc = transaction.get(userRef)
                val currentLiked = userDoc.get("likedAlbums") as? List<String> ?: emptyList()
                val updatedLiked = currentLiked - albumId
                transaction.update(userRef, "likedAlbums", updatedLiked)
            }.await()

            Log.d("FirebaseRepository", "✅ Album unliked successfully")
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error unliking album: ${e.message}")
            false
        }
    }

    suspend fun getLikedAlbums(userId: String): List<String> {
        return try {
            val user = getUser(userId)
            val likedAlbums = user?.likedAlbums ?: emptyList()
            Log.d("FirebaseRepository", "📖 User $userId has ${likedAlbums.size} liked albums")
            likedAlbums
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error getting liked albums: ${e.message}")
            emptyList()
        }
    }

    suspend fun likeReview(userId: String, reviewId: String): Boolean {
        return try {
            Log.d("FirebaseRepository", "❤️ User $userId liking review: $reviewId")

            val likeData = mapOf<String, Any>(
                "userId" to userId,
                "reviewId" to reviewId,
                "timestamp" to System.currentTimeMillis(),
                "type" to "review"
            )
            db.collection(LIKES_COLLECTION).document("${userId}_review_$reviewId").set(likeData).await()

            val userRef = db.collection(USERS_COLLECTION).document(userId)
            val userDoc = userRef.get().await()

            if (userDoc.exists()) {
                db.runTransaction { transaction ->
                    val existingUserDoc = transaction.get(userRef)
                    val currentLiked = existingUserDoc.get("likedReviews") as? List<String> ?: emptyList()
                    val updatedLiked = (currentLiked + reviewId).distinct()
                    transaction.update(userRef, "likedReviews", updatedLiked)
                }.await()
            } else {
                val newUser = FirestoreUser(
                    uid = userId,
                    username = "User",
                    email = "",
                    reviewedAlbums = emptyList(),
                    favoriteAlbums = emptyList(),
                    likedAlbums = emptyList(),
                    likedReviews = listOf(reviewId),
                    likedLists = emptyList(),
                    createdLists = emptyList()
                )
                userRef.set(newUser).await()
            }

            Log.d("FirebaseRepository", "✅ Review liked successfully")
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error liking review: ${e.message}")
            false
        }
    }

    suspend fun unlikeReview(userId: String, reviewId: String): Boolean {
        return try {
            Log.d("FirebaseRepository", "💔 User $userId unliking review: $reviewId")

            db.collection(LIKES_COLLECTION).document("${userId}_review_$reviewId").delete().await()

            val userRef = db.collection(USERS_COLLECTION).document(userId)
            db.runTransaction { transaction ->
                val userDoc = transaction.get(userRef)
                val currentLiked = userDoc.get("likedReviews") as? List<String> ?: emptyList()
                val updatedLiked = currentLiked - reviewId
                transaction.update(userRef, "likedReviews", updatedLiked)
            }.await()

            Log.d("FirebaseRepository", "✅ Review unliked successfully")
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error unliking review: ${e.message}")
            false
        }
    }

    suspend fun getLikedReviews(userId: String): List<String> {
        return try {
            val user = getUser(userId)
            val likedReviews = user?.likedReviews ?: emptyList()
            Log.d("FirebaseRepository", "📖 User $userId has ${likedReviews.size} liked reviews")
            likedReviews
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error getting liked reviews: ${e.message}")
            emptyList()
        }
    }

    suspend fun likeList(userId: String, listId: String): Boolean {
        return try {
            Log.d("FirebaseRepository", "❤️ User $userId liking list: $listId")

            val likeData = mapOf<String, Any>(
                "userId" to userId,
                "listId" to listId,
                "timestamp" to System.currentTimeMillis(),
                "type" to "list"
            )
            db.collection(LIKES_COLLECTION).document("${userId}_list_$listId").set(likeData).await()

            val userRef = db.collection(USERS_COLLECTION).document(userId)
            val userDoc = userRef.get().await()

            if (userDoc.exists()) {
                db.runTransaction { transaction ->
                    val existingUserDoc = transaction.get(userRef)
                    val currentLiked = existingUserDoc.get("likedLists") as? List<String> ?: emptyList()
                    val updatedLiked = (currentLiked + listId).distinct()
                    transaction.update(userRef, "likedLists", updatedLiked)
                }.await()
            } else {
                val newUser = FirestoreUser(
                    uid = userId,
                    username = "User",
                    email = "",
                    reviewedAlbums = emptyList(),
                    favoriteAlbums = emptyList(),
                    likedAlbums = emptyList(),
                    likedReviews = emptyList(),
                    likedLists = listOf(listId),
                    createdLists = emptyList()
                )
                userRef.set(newUser).await()
            }

            Log.d("FirebaseRepository", "✅ List liked successfully")
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error liking list: ${e.message}")
            false
        }
    }

    suspend fun unlikeList(userId: String, listId: String): Boolean {
        return try {
            Log.d("FirebaseRepository", "💔 User $userId unliking list: $listId")

            db.collection(LIKES_COLLECTION).document("${userId}_list_$listId").delete().await()

            val userRef = db.collection(USERS_COLLECTION).document(userId)
            db.runTransaction { transaction ->
                val userDoc = transaction.get(userRef)
                val currentLiked = userDoc.get("likedLists") as? List<String> ?: emptyList()
                val updatedLiked = currentLiked - listId
                transaction.update(userRef, "likedLists", updatedLiked)
            }.await()

            Log.d("FirebaseRepository", "✅ List unliked successfully")
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error unliking list: ${e.message}")
            false
        }
    }

    suspend fun getLikedLists(userId: String): List<String> {
        return try {
            val user = getUser(userId)
            val likedLists = user?.likedLists ?: emptyList()
            Log.d("FirebaseRepository", "📖 User $userId has ${likedLists.size} liked lists")
            likedLists
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error getting liked lists: ${e.message}")
            emptyList()
        }
    }

    // ========== FAVORITE ALBUMS OPERATIONS (COMPLETE BACKWARD COMPATIBILITY) ==========

    // OLD format - for backward compatibility
    suspend fun updateFavoriteAlbums(userId: String, albumIds: List<String>): Boolean {
        return try {
            Log.d("FirebaseRepository", "💾 Storing ${albumIds.size} album IDs for user: $userId")
            val updates = mapOf<String, Any>("favoriteAlbums" to albumIds)
            db.collection(USERS_COLLECTION).document(userId).update(updates).await()
            Log.d("FirebaseRepository", "✅ Album IDs stored successfully")
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error storing album IDs: ${e.message}")
            false
        }
    }

    // NEW format - stores full album data
    suspend fun updateFavoriteAlbumsFull(userId: String, albums: List<Music>): Boolean {
        return try {
            Log.d("FirebaseRepository", "💾 Storing ${albums.size} full albums for user: $userId")

            val albumMaps = albums.map { album ->
                mapOf(
                    "id" to album.id,
                    "title" to album.title,
                    "artist" to album.artist,
                    "album" to album.album,
                    "releaseYear" to album.releaseYear,
                    "genre" to album.genre,
                    "coverImage" to album.coverImage,
                    "averageRating" to album.averageRating,
                    "reviewCount" to album.reviewCount
                )
            }

            val updates = mapOf<String, Any>("favoriteAlbumsFull" to albumMaps)
            db.collection(USERS_COLLECTION).document(userId).update(updates).await()
            Log.d("FirebaseRepository", "✅ Full albums stored successfully")
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error storing full albums: ${e.message}")
            false
        }
    }

    // Update both formats simultaneously for complete compatibility
    suspend fun updateFavoriteAlbumsBothFormats(userId: String, albums: List<Music>): Boolean {
        return try {
            Log.d("FirebaseRepository", "💾 Storing ${albums.size} albums in BOTH formats for user: $userId")

            val albumIds = albums.map { it.id }
            val albumMaps = albums.map { album ->
                mapOf(
                    "id" to album.id,
                    "title" to album.title,
                    "artist" to album.artist,
                    "album" to album.album,
                    "releaseYear" to album.releaseYear,
                    "genre" to album.genre,
                    "coverImage" to album.coverImage,
                    "averageRating" to album.averageRating,
                    "reviewCount" to album.reviewCount
                )
            }

            val updates = mapOf<String, Any>(
                "favoriteAlbums" to albumIds,
                "favoriteAlbumsFull" to albumMaps
            )

            db.collection(USERS_COLLECTION).document(userId).update(updates).await()
            Log.d("FirebaseRepository", "✅ Albums stored in BOTH formats successfully")
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error storing albums in both formats: ${e.message}")
            false
        }
    }

    // THE KEY FIX: Retrieval with complete backward compatibility
    suspend fun getFavoriteAlbumsWithDetails(userId: String): List<Music> {
        return try {
            val userDoc = db.collection(USERS_COLLECTION).document(userId).get().await()

            // Try NEW format first (full album data)
            val favoriteAlbumsFull = userDoc.get("favoriteAlbumsFull") as? List<Map<String, Any>>
            if (favoriteAlbumsFull != null && favoriteAlbumsFull.isNotEmpty()) {
                Log.d("FirebaseRepository", "📖 Found ${favoriteAlbumsFull.size} albums in NEW format")
                val favoriteAlbums = favoriteAlbumsFull.mapNotNull { albumMap ->
                    try {
                        Music(
                            id = albumMap["id"] as? String ?: "",
                            title = albumMap["title"] as? String ?: "",
                            artist = albumMap["artist"] as? String ?: "",
                            album = albumMap["album"] as? String ?: "",
                            releaseYear = (albumMap["releaseYear"] as? Long)?.toInt() ?: 0,
                            genre = albumMap["genre"] as? String ?: "",
                            coverImage = albumMap["coverImage"] as? String ?: "",
                            averageRating = (albumMap["averageRating"] as? Double) ?: 0.0,
                            reviewCount = (albumMap["reviewCount"] as? Long)?.toInt() ?: 0
                        )
                    } catch (e: Exception) {
                        Log.e("FirebaseRepository", "❌ Error parsing album: ${e.message}")
                        null
                    }
                }
                Log.d("FirebaseRepository", "📖 Returning ${favoriteAlbums.size} albums from NEW format")
                return favoriteAlbums
            }

            // Fall back to OLD format (just IDs)
            val favoriteAlbumIds = userDoc.get("favoriteAlbums") as? List<String> ?: emptyList()
            Log.d("FirebaseRepository", "📖 Found ${favoriteAlbumIds.size} albums in OLD format (IDs)")

            val favoriteAlbums = mutableListOf<Music>()
            for (albumId in favoriteAlbumIds) {
                val album = getAlbumById(albumId)
                if (album != null) {
                    favoriteAlbums.add(album)
                } else {
                    Log.w("FirebaseRepository", "⚠️ Album not found in albums collection: $albumId")
                }
            }

            Log.d("FirebaseRepository", "📖 Returning ${favoriteAlbums.size} albums from OLD format")
            favoriteAlbums
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error getting favorite albums: ${e.message}")
            emptyList()
        }
    }

    // Get favorite albums in both formats for maximum compatibility
    suspend fun getFavoriteAlbumsBothFormats(userId: String): Map<String, Any> {
        return try {
            val userDoc = db.collection(USERS_COLLECTION).document(userId).get().await()

            val favoriteAlbumsFull = userDoc.get("favoriteAlbumsFull") as? List<Map<String, Any>> ?: emptyList()
            val favoriteAlbumIds = userDoc.get("favoriteAlbums") as? List<String> ?: emptyList()

            val fullAlbums = favoriteAlbumsFull.mapNotNull { albumMap ->
                try {
                    Music(
                        id = albumMap["id"] as? String ?: "",
                        title = albumMap["title"] as? String ?: "",
                        artist = albumMap["artist"] as? String ?: "",
                        album = albumMap["album"] as? String ?: "",
                        releaseYear = (albumMap["releaseYear"] as? Long)?.toInt() ?: 0,
                        genre = albumMap["genre"] as? String ?: "",
                        coverImage = albumMap["coverImage"] as? String ?: "",
                        averageRating = (albumMap["averageRating"] as? Double) ?: 0.0,
                        reviewCount = (albumMap["reviewCount"] as? Long)?.toInt() ?: 0
                    )
                } catch (e: Exception) {
                    null
                }
            }

            mapOf(
                "fullAlbums" to fullAlbums,
                "albumIds" to favoriteAlbumIds,
                "hasNewFormat" to favoriteAlbumsFull.isNotEmpty(),
                "hasOldFormat" to favoriteAlbumIds.isNotEmpty()
            )
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error getting favorite albums in both formats: ${e.message}")
            emptyMap()
        }
    }

    suspend fun getFavoriteAlbums(userId: String): List<String> {
        return try {
            val user = getUser(userId)
            val favoriteAlbums = user?.favoriteAlbums ?: emptyList()
            Log.d("FirebaseRepository", "📖 User $userId has ${favoriteAlbums.size} favorite album IDs")
            favoriteAlbums
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error getting favorite album IDs: ${e.message}")
            emptyList()
        }
    }

    suspend fun addFavoriteAlbum(userId: String, albumId: String): Boolean {
        return try {
            Log.d("FirebaseRepository", "⭐ Adding favorite album: $albumId for user: $userId")
            val updates = mapOf<String, Any>("favoriteAlbums" to FieldValue.arrayUnion(albumId))
            db.collection(USERS_COLLECTION).document(userId).update(updates).await()
            Log.d("FirebaseRepository", "✅ Favorite album added successfully")
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error adding favorite album: ${e.message}")
            false
        }
    }

    suspend fun removeFavoriteAlbum(userId: String, albumId: String): Boolean {
        return try {
            Log.d("FirebaseRepository", "🗑️ Removing favorite album: $albumId for user: $userId")
            val updates = mapOf<String, Any>("favoriteAlbums" to FieldValue.arrayRemove(albumId))
            db.collection(USERS_COLLECTION).document(userId).update(updates).await()
            Log.d("FirebaseRepository", "✅ Favorite album removed successfully")
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error removing favorite album: ${e.message}")
            false
        }
    }

    suspend fun setFavoriteAlbum(userId: String, album: Music, position: Int? = null): Boolean {
        return try {
            val currentFavorites = getFavoriteAlbumsWithDetails(userId).toMutableList()

            if (position != null && position < currentFavorites.size) {
                currentFavorites[position] = album
            } else {
                if (currentFavorites.size < 4) {
                    currentFavorites.add(album)
                } else {
                    currentFavorites[3] = album
                }
            }

            // Update in both formats for maximum compatibility
            updateFavoriteAlbumsBothFormats(userId, currentFavorites.distinctBy { it.id })
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error setting favorite album: ${e.message}")
            false
        }
    }

    suspend fun removeFavoriteAlbumByPosition(userId: String, position: Int): Boolean {
        return try {
            val currentFavorites = getFavoriteAlbumsWithDetails(userId).toMutableList()
            if (position < currentFavorites.size) {
                currentFavorites.removeAt(position)
                // Update in both formats for maximum compatibility
                updateFavoriteAlbumsBothFormats(userId, currentFavorites)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error removing favorite album by position: ${e.message}")
            false
        }
    }

    suspend fun getFavoriteAlbumsCount(userId: String): Int {
        return try {
            val count = getFavoriteAlbumsWithDetails(userId).size
            Log.d("FirebaseRepository", "📊 User $userId has $count favorite albums")
            count
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error getting favorite albums count: ${e.message}")
            0
        }
    }

    // ========== ALBUM DATA OPERATIONS ==========

    suspend fun saveAlbum(album: Music): Boolean {
        return try {
            Log.d("FirebaseRepository", "💾 Saving album: ${album.title} (${album.id})")
            db.collection(ALBUMS_COLLECTION).document(album.id).set(album).await()
            Log.d("FirebaseRepository", "✅ Album saved successfully: ${album.title}")
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error saving album: ${e.message}")
            false
        }
    }

    suspend fun getAlbumById(albumId: String): Music? {
        return try {
            val albumDoc = db.collection(ALBUMS_COLLECTION).document(albumId).get().await()
            if (albumDoc.exists()) {
                albumDoc.toObject(Music::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error getting album: ${e.message}")
            null
        }
    }

    suspend fun searchAlbums(query: String): List<Music> {
        return try {
            val albums = db.collection(ALBUMS_COLLECTION)
                .whereGreaterThanOrEqualTo("title", query)
                .whereLessThanOrEqualTo("title", query + "\uf8ff")
                .get()
                .await()
                .toObjects(Music::class.java)
            Log.d("FirebaseRepository", "🔍 Found ${albums.size} albums for query: $query")
            albums
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error searching albums: ${e.message}")
            emptyList()
        }
    }

    // ========== PLAYLIST OPERATIONS ==========

    suspend fun addToPlaylist(userId: String, music: Music): Boolean {
        return try {
            Log.d("FirebaseRepository", "🎵 Adding to playlist: ${music.title} for user: $userId")

            // Save album first
            saveAlbum(music)

            // Add to user's playlist
            val updates = mapOf<String, Any>("playlistAlbums" to FieldValue.arrayUnion(music.id))
            db.collection(USERS_COLLECTION).document(userId).update(updates).await()
            Log.d("FirebaseRepository", "✅ Added to playlist successfully")
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error adding to playlist: ${e.message}")
            false
        }
    }

    suspend fun removeFromPlaylist(userId: String, musicId: String): Boolean {
        return try {
            Log.d("FirebaseRepository", "🗑️ Removing from playlist: $musicId for user: $userId")
            val updates = mapOf<String, Any>("playlistAlbums" to FieldValue.arrayRemove(musicId))
            db.collection(USERS_COLLECTION).document(userId).update(updates).await()
            Log.d("FirebaseRepository", "✅ Removed from playlist successfully")
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error removing from playlist: ${e.message}")
            false
        }
    }

    suspend fun getPlaylist(userId: String): List<Music> {
        return try {
            val userDoc = db.collection(USERS_COLLECTION).document(userId).get().await()
            val playlistAlbumIds = userDoc.get("playlistAlbums") as? List<String> ?: emptyList()

            val playlist = mutableListOf<Music>()
            for (albumId in playlistAlbumIds) {
                val album = getAlbumById(albumId)
                if (album != null) {
                    playlist.add(album)
                }
            }
            Log.d("FirebaseRepository", "📖 Retrieved ${playlist.size} playlist items for user: $userId")
            playlist
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error getting playlist: ${e.message}")
            emptyList()
        }
    }

    suspend fun isInPlaylist(userId: String, musicId: String): Boolean {
        return try {
            val userDoc = db.collection(USERS_COLLECTION).document(userId).get().await()
            val playlistAlbumIds = userDoc.get("playlistAlbums") as? List<String> ?: emptyList()
            val isInPlaylist = playlistAlbumIds.contains(musicId)
            Log.d("FirebaseRepository", "🔍 Music $musicId in playlist: $isInPlaylist")
            isInPlaylist
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error checking playlist: ${e.message}")
            false
        }
    }

    // ========== UTILITY METHODS ==========

    suspend fun isAlbumLiked(userId: String, albumId: String): Boolean {
        return try {
            val likedAlbums = getLikedAlbums(userId)
            val isLiked = likedAlbums.contains(albumId)
            Log.d("FirebaseRepository", "🔍 Album $albumId liked by user $userId: $isLiked")
            isLiked
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error checking if album is liked: ${e.message}")
            false
        }
    }

    suspend fun isReviewLiked(userId: String, reviewId: String): Boolean {
        return try {
            val likedReviews = getLikedReviews(userId)
            val isLiked = likedReviews.contains(reviewId)
            Log.d("FirebaseRepository", "🔍 Review $reviewId liked by user $userId: $isLiked")
            isLiked
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error checking if review is liked: ${e.message}")
            false
        }
    }

    suspend fun isListLiked(userId: String, listId: String): Boolean {
        return try {
            val likedLists = getLikedLists(userId)
            val isLiked = likedLists.contains(listId)
            Log.d("FirebaseRepository", "🔍 List $listId liked by user $userId: $isLiked")
            isLiked
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error checking if list is liked: ${e.message}")
            false
        }
    }

    suspend fun getUserStats(userId: String): Map<String, Int> {
        return try {
            val favoriteAlbumsCount = getFavoriteAlbumsCount(userId)
            val userReviews = getReviewsByUser(userId)
            val userLists = getUserLists(userId)
            val likedAlbums = getLikedAlbums(userId)

            val stats = mapOf(
                "favoriteAlbums" to favoriteAlbumsCount,
                "reviews" to userReviews.size,
                "lists" to userLists.size,
                "likedAlbums" to likedAlbums.size
            )
            Log.d("FirebaseRepository", "📊 User $userId stats: $stats")
            stats
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error getting user stats: ${e.message}")
            emptyMap()
        }
    }

    suspend fun updateUserProfileFields(userId: String, profileData: Map<String, Any>): Boolean {
        return try {
            val updates = mutableMapOf<String, Any>()

            if (profileData.containsKey("bio")) {
                updates["bio"] = profileData["bio"] as String
            }
            if (profileData.containsKey("location")) {
                updates["location"] = profileData["location"] as String
            }
            if (profileData.containsKey("website")) {
                updates["website"] = profileData["website"] as String
            }
            if (profileData.containsKey("profileImageUrl")) {
                updates["profileImageUrl"] = profileData["profileImageUrl"] as String
            }

            if (updates.isNotEmpty()) {
                db.collection(USERS_COLLECTION).document(userId).update(updates).await()
                Log.d("FirebaseRepository", "✅ User profile updated successfully")
                true
            } else {
                Log.d("FirebaseRepository", "⚠️ No profile fields to update")
                false
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error updating user profile: ${e.message}")
            false
        }
    }

    suspend fun getUserRecentActivity(userId: String): List<Any> {
        return try {
            val reviews = getReviewsByUser(userId)
            val lists = getUserLists(userId)

            val activity = mutableListOf<Any>()
            activity.addAll(reviews)
            activity.addAll(lists)

            val sortedActivity = activity.sortedByDescending {
                when (it) {
                    is Review -> it.timestamp
                    is UserList -> it.createdAt
                    else -> ""
                }
            }.take(10)

            Log.d("FirebaseRepository", "📖 Retrieved ${sortedActivity.size} recent activities for user: $userId")
            sortedActivity
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error getting recent activity: ${e.message}")
            emptyList()
        }
    }

    suspend fun getUserDataBatch(userId: String): Map<String, Any> {
        return try {
            val userDoc = db.collection(USERS_COLLECTION).document(userId).get().await()
            val reviewsQuery = db.collection(REVIEWS_COLLECTION).whereEqualTo("userId", userId).get().await()
            val listsQuery = db.collection(LISTS_COLLECTION).whereEqualTo("creator", userId).get().await()

            val batchData = mapOf(
                "user" to (userDoc.toObject(FirestoreUser::class.java) ?: FirestoreUser()),
                "reviews" to reviewsQuery.toObjects(Review::class.java),
                "lists" to listsQuery.toObjects(UserList::class.java)
            )
            Log.d("FirebaseRepository", "📦 Retrieved batch data for user: $userId")
            batchData
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error getting batch data: ${e.message}")
            emptyMap()
        }
    }

    // ========== BACKWARD COMPATIBILITY UTILITIES ==========

    suspend fun migrateUserToNewFormat(userId: String): Boolean {
        return try {
            Log.d("FirebaseRepository", "🔄 Migrating user $userId to new format")

            val userData = getUserWithFullData(userId)
            val favoriteAlbumIds = userData["favoriteAlbums"] as? List<String> ?: emptyList()

            if (favoriteAlbumIds.isNotEmpty()) {
                // Convert old format to new format
                val albums = mutableListOf<Music>()
                for (albumId in favoriteAlbumIds) {
                    val album = getAlbumById(albumId)
                    if (album != null) {
                        albums.add(album)
                    }
                }

                if (albums.isNotEmpty()) {
                    updateFavoriteAlbumsBothFormats(userId, albums)
                    Log.d("FirebaseRepository", "✅ Migrated ${albums.size} albums to new format")
                    return true
                }
            }

            Log.d("FirebaseRepository", "⚠️ No albums to migrate for user: $userId")
            false
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error migrating user to new format: ${e.message}")
            false
        }
    }

    suspend fun checkDataCompatibility(userId: String): Map<String, Any> {
        return try {
            val userDoc = db.collection(USERS_COLLECTION).document(userId).get().await()

            val hasNewFormat = userDoc.get("favoriteAlbumsFull") != null
            val hasOldFormat = userDoc.get("favoriteAlbums") != null
            val newFormatCount = (userDoc.get("favoriteAlbumsFull") as? List<*>)?.size ?: 0
            val oldFormatCount = (userDoc.get("favoriteAlbums") as? List<*>)?.size ?: 0

            mapOf(
                "hasNewFormat" to hasNewFormat,
                "hasOldFormat" to hasOldFormat,
                "newFormatCount" to newFormatCount,
                "oldFormatCount" to oldFormatCount,
                "needsMigration" to (hasOldFormat && !hasNewFormat)
            )
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "❌ Error checking data compatibility: ${e.message}")
            emptyMap()
        }
    }
}