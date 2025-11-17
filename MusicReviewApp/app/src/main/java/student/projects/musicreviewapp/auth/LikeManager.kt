package student.projects.musicreviewapp.auth

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import student.projects.musicreviewapp.models.Music
import student.projects.musicreviewapp.models.Review
import student.projects.musicreviewapp.models.UserList

class LikeManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("likes_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Keys for shared preferences
    private val keyLikedAlbums = "liked_albums"
    private val keyLikedReviews = "liked_reviews"
    private val keyLikedLists = "liked_lists"

    // Album Likes
    fun likeAlbum(album: Music) {
        val likedAlbums = getLikedAlbums().toMutableList()
        if (!likedAlbums.any { it.id == album.id }) {
            likedAlbums.add(album)
            saveLikedAlbums(likedAlbums)
        }
    }

    fun unlikeAlbum(albumId: String) {
        val likedAlbums = getLikedAlbums().toMutableList()
        likedAlbums.removeAll { it.id == albumId }
        saveLikedAlbums(likedAlbums)
    }

    fun getLikedAlbums(): List<Music> {
        val json = sharedPreferences.getString(keyLikedAlbums, null)
        return if (json != null) {
            gson.fromJson(json, Array<Music>::class.java).toList()
        } else {
            emptyList()
        }
    }

    fun isAlbumLiked(albumId: String): Boolean {
        return getLikedAlbums().any { it.id == albumId }
    }

    // Review Likes
    fun likeReview(review: Review) {
        val likedReviews = getLikedReviews().toMutableList()
        if (!likedReviews.any { it.id == review.id }) {
            likedReviews.add(review)
            saveLikedReviews(likedReviews)
        }
    }

    fun unlikeReview(reviewId: String) {
        val likedReviews = getLikedReviews().toMutableList()
        likedReviews.removeAll { it.id == reviewId }
        saveLikedReviews(likedReviews)
    }

    fun getLikedReviews(): List<Review> {
        val json = sharedPreferences.getString(keyLikedReviews, null)
        return if (json != null) {
            gson.fromJson(json, Array<Review>::class.java).toList()
        } else {
            emptyList()
        }
    }

    fun isReviewLiked(reviewId: String): Boolean {
        return getLikedReviews().any { it.id == reviewId }
    }

    // List Likes
    fun likeList(userList: UserList) {
        val likedLists = getLikedLists().toMutableList()
        if (!likedLists.any { it.id == userList.id }) {
            likedLists.add(userList)
            saveLikedLists(likedLists)
        }
    }

    fun unlikeList(listId: String) {
        val likedLists = getLikedLists().toMutableList()
        likedLists.removeAll { it.id == listId }
        saveLikedLists(likedLists)
    }

    fun getLikedLists(): List<UserList> {
        val json = sharedPreferences.getString(keyLikedLists, null)
        return if (json != null) {
            gson.fromJson(json, Array<UserList>::class.java).toList()
        } else {
            emptyList()
        }
    }

    fun isListLiked(listId: String): Boolean {
        return getLikedLists().any { it.id == listId }
    }

    // Clear all likes (for testing)
    fun clearAllLikes() {
        sharedPreferences.edit().remove(keyLikedAlbums).remove(keyLikedReviews).remove(keyLikedLists).apply()
    }

    // Private save methods
    private fun saveLikedAlbums(albums: List<Music>) {
        val json = gson.toJson(albums)
        sharedPreferences.edit().putString(keyLikedAlbums, json).apply()
    }

    private fun saveLikedReviews(reviews: List<Review>) {
        val json = gson.toJson(reviews)
        sharedPreferences.edit().putString(keyLikedReviews, json).apply()
    }

    private fun saveLikedLists(lists: List<UserList>) {
        val json = gson.toJson(lists)
        sharedPreferences.edit().putString(keyLikedLists, json).apply()
    }
}