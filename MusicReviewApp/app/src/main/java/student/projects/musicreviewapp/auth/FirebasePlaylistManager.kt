package student.projects.musicreviewapp.auth

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import student.projects.musicreviewapp.models.Music
import student.projects.musicreviewapp.repositories.FirebaseRepository

class FirebasePlaylistManager(private val context: Context) {

    private val repository = FirebaseRepository()
    private val authManager = AuthManager()
    private val localManager = PlaylistManager(context)

    fun addToPlaylist(music: Music, onComplete: (Boolean) -> Unit = {}) {
        val userId = authManager.getCurrentUid()

        // Save locally first for immediate UI update
        localManager.addToPlaylist(music)

        if (userId != null) {
            // Sync with Firebase
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    repository.addToPlaylist(userId, music)
                    CoroutineScope(Dispatchers.Main).launch {
                        onComplete(true)
                    }
                } catch (e: Exception) {
                    // Firebase failed, but local save worked
                    CoroutineScope(Dispatchers.Main).launch {
                        onComplete(true)
                    }
                }
            }
        } else {
            onComplete(true)
        }
    }

    fun removeFromPlaylist(musicId: String, onComplete: (Boolean) -> Unit = {}) {
        val userId = authManager.getCurrentUid()

        // Remove locally first
        localManager.removeFromPlaylist(musicId)

        if (userId != null) {
            // Sync with Firebase
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    repository.removeFromPlaylist(userId, musicId)
                    CoroutineScope(Dispatchers.Main).launch {
                        onComplete(true)
                    }
                } catch (e: Exception) {
                    // Firebase failed, but local removal worked
                    CoroutineScope(Dispatchers.Main).launch {
                        onComplete(true)
                    }
                }
            }
        } else {
            onComplete(true)
        }
    }

    fun getPlaylist(onResult: (List<Music>) -> Unit) {
        val userId = authManager.getCurrentUid()

        if (userId != null) {
            // Try Firebase first
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val firebasePlaylist = repository.getPlaylist(userId)
                    CoroutineScope(Dispatchers.Main).launch {
                        onResult(firebasePlaylist)
                    }
                } catch (e: Exception) {
                    // Fallback to local storage
                    val localPlaylist = localManager.getPlaylist()
                    CoroutineScope(Dispatchers.Main).launch {
                        onResult(localPlaylist)
                    }
                }
            }
        } else {
            // No user logged in, use local storage
            val localPlaylist = localManager.getPlaylist()
            onResult(localPlaylist)
        }
    }

    fun isInPlaylist(musicId: String, onResult: (Boolean) -> Unit) {
        val userId = authManager.getCurrentUid()

        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val isInPlaylist = repository.isInPlaylist(userId, musicId)
                    CoroutineScope(Dispatchers.Main).launch {
                        onResult(isInPlaylist)
                    }
                } catch (e: Exception) {
                    // Fallback to local storage
                    val isInPlaylist = localManager.isInPlaylist(musicId)
                    CoroutineScope(Dispatchers.Main).launch {
                        onResult(isInPlaylist)
                    }
                }
            }
        } else {
            val isInPlaylist = localManager.isInPlaylist(musicId)
            onResult(isInPlaylist)
        }
    }

    // Sync local playlist to Firebase (for migration)
    fun syncLocalPlaylistToFirebase(onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        val userId = authManager.getCurrentUid() ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val localPlaylist = localManager.getPlaylist()
                localPlaylist.forEach { music ->
                    repository.addToPlaylist(userId, music)
                }

                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(true, "Playlist synced successfully")
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete(false, "Failed to sync playlist: ${e.message}")
                }
            }
        }
    }
}