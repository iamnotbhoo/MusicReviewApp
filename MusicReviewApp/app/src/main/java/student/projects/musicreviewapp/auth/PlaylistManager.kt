package student.projects.musicreviewapp.auth

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import student.projects.musicreviewapp.models.Music

class PlaylistManager(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences("music_review_app", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val playlistKey = "user_playlist"

    fun addToPlaylist(music: Music) {
        val currentPlaylist = getPlaylist().toMutableList()

        // Check if album already exists in playlist
        if (!currentPlaylist.any { it.id == music.id }) {
            currentPlaylist.add(music)
            savePlaylist(currentPlaylist)
        }
    }

    fun removeFromPlaylist(musicId: String) {
        val currentPlaylist = getPlaylist().toMutableList()
        currentPlaylist.removeAll { it.id == musicId }
        savePlaylist(currentPlaylist)
    }

    fun getPlaylist(): List<Music> {
        val playlistJson = sharedPreferences.getString(playlistKey, null)
        return if (playlistJson != null) {
            val type = object : TypeToken<List<Music>>() {}.type
            gson.fromJson(playlistJson, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun isInPlaylist(musicId: String): Boolean {
        return getPlaylist().any { it.id == musicId }
    }

    private fun savePlaylist(playlist: List<Music>) {
        val playlistJson = gson.toJson(playlist)
        sharedPreferences.edit().putString(playlistKey, playlistJson).apply()
    }
}