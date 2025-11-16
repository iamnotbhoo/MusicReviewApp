package student.projects.musicreviewapp.auth

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import student.projects.musicreviewapp.models.Music

class FavoriteAlbumsManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("favorite_albums", Context.MODE_PRIVATE)
    private val key = "favorite_albums_list"

    fun getFavoriteAlbums(): List<Music> {
        val jsonString = sharedPreferences.getString(key, null)
        return if (jsonString != null) {
            parseAlbumsFromJson(jsonString)
        } else {
            emptyList()
        }
    }

    fun saveFavoriteAlbums(albums: List<Music>) {
        val jsonString = convertAlbumsToJson(albums)
        sharedPreferences.edit().putString(key, jsonString).apply()
    }

    fun addFavoriteAlbum(album: Music) {
        val currentAlbums = getFavoriteAlbums().toMutableList()
        if (currentAlbums.size >= 4) {
            throw IllegalStateException("Cannot add more than 4 favorite albums")
        }
        if (!currentAlbums.any { it.id == album.id }) {
            currentAlbums.add(album)
            saveFavoriteAlbums(currentAlbums)
        }
    }

    fun removeFavoriteAlbum(albumId: String) {
        val currentAlbums = getFavoriteAlbums().toMutableList()
        currentAlbums.removeAll { it.id == albumId }
        saveFavoriteAlbums(currentAlbums)
    }

    fun updateFavoriteAlbums(newAlbums: List<Music>) {
        if (newAlbums.size > 4) {
            throw IllegalStateException("Cannot have more than 4 favorite albums")
        }
        saveFavoriteAlbums(newAlbums)
    }

    fun hasEmptySlots(): Boolean {
        return getFavoriteAlbums().size < 4
    }

    fun getEmptySlotsCount(): Int {
        return 4 - getFavoriteAlbums().size
    }

    private fun convertAlbumsToJson(albums: List<Music>): String {
        val jsonArray = JSONArray()
        albums.forEach { album ->
            val jsonObject = JSONObject().apply {
                put("id", album.id)
                put("title", album.title)
                put("artist", album.artist)
                put("album", album.album)
                put("releaseYear", album.releaseYear)
                put("genre", album.genre)
                put("coverImage", album.coverImage)
                put("averageRating", album.averageRating)
                put("reviewCount", album.reviewCount)
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    private fun parseAlbumsFromJson(jsonString: String): List<Music> {
        val albums = mutableListOf<Music>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val album = Music(
                    id = jsonObject.getString("id"),
                    title = jsonObject.getString("title"),
                    artist = jsonObject.getString("artist"),
                    album = jsonObject.getString("album"),
                    releaseYear = jsonObject.getInt("releaseYear"),
                    genre = jsonObject.getString("genre"),
                    coverImage = jsonObject.getString("coverImage"),
                    averageRating = jsonObject.getDouble("averageRating"),
                    reviewCount = jsonObject.getInt("reviewCount")
                )
                albums.add(album)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return albums
    }
}