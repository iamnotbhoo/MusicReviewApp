package student.projects.musicreviewapp.auth

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import student.projects.musicreviewapp.models.Music
import student.projects.musicreviewapp.repositories.FirebaseRepository

class FirebaseFavoriteAlbumsManager(private val context: Context) {

    private val repository = FirebaseRepository()
    private val authManager = AuthManager()
    private val localManager = FavoriteAlbumsManager(context)

    fun getFavoriteAlbums(onResult: (List<Music>) -> Unit) {
        val userId = authManager.getCurrentUid()

        if (userId != null) {
            // Try Firebase first
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val firebaseAlbums = repository.getFavoriteAlbumsWithDetails(userId)
                    CoroutineScope(Dispatchers.Main).launch {
                        Log.d("FavoriteAlbums", "Loaded ${firebaseAlbums.size} albums from Firebase")
                        onResult(firebaseAlbums)
                    }
                } catch (e: Exception) {
                    Log.e("FavoriteAlbums", "Firebase error: ${e.message}", e)
                    // Fallback to local storage
                    val localAlbums = localManager.getFavoriteAlbums()
                    CoroutineScope(Dispatchers.Main).launch {
                        Log.d("FavoriteAlbums", "Loaded ${localAlbums.size} albums from local storage")
                        onResult(localAlbums)
                    }
                }
            }
        } else {
            // No user logged in, use local storage
            Log.d("FavoriteAlbums", "No user logged in, using local storage")
            val localAlbums = localManager.getFavoriteAlbums()
            onResult(localAlbums)
        }
    }

    fun updateFavoriteAlbums(albums: List<Music>, onComplete: (Boolean) -> Unit = {}) {
        val userId = authManager.getCurrentUid()

        // Save locally first for immediate UI update
        localManager.saveFavoriteAlbums(albums)

        if (userId != null) {
            // Sync with Firebase
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val albumIds = albums.map { album -> album.id }

                    // Save album data first
                    albums.forEach { album ->
                        repository.saveAlbum(album)
                    }

                    // Update favorite albums list
                    repository.updateFavoriteAlbums(userId, albumIds)

                    CoroutineScope(Dispatchers.Main).launch {
                        Log.d("FavoriteAlbums", "Successfully updated ${albums.size} albums in Firebase")
                        onComplete(true)
                    }
                } catch (e: Exception) {
                    Log.e("FavoriteAlbums", "Firebase update failed: ${e.message}", e)
                    // Firebase failed, but local save worked
                    CoroutineScope(Dispatchers.Main).launch {
                        onComplete(true) // Still true because local save worked
                    }
                }
            }
        } else {
            Log.d("FavoriteAlbums", "No user logged in, only saved locally")
            onComplete(true)
        }
    }

    // Add this method for debugging
    fun debugFavoriteAlbums() {
        getFavoriteAlbums { albums ->
            Log.d("FavoriteAlbums", "Current favorites: ${albums.size} albums")
            albums.forEachIndexed { index, album ->
                Log.d("FavoriteAlbums", "Album $index: ${album.title} by ${album.artist} (ID: ${album.id})")
            }
        }
    }
}