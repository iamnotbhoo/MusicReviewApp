package student.projects.musicreviewapp.auth

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import student.projects.musicreviewapp.models.UserList
import student.projects.musicreviewapp.models.Music
import java.text.SimpleDateFormat
import java.util.*

class ListManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_lists", Context.MODE_PRIVATE)
    private val key = "user_lists_data"

    fun createList(list: UserList) {
        val currentLists = getLists().toMutableList()
        currentLists.add(list)
        saveListsToStorage(currentLists)
    }

    fun getLists(): List<UserList> {
        val jsonString = sharedPreferences.getString(key, null)
        return if (jsonString != null) {
            parseListsFromJson(jsonString)
        } else {
            emptyList()
        }
    }

    fun getListById(listId: String): UserList? {
        return getLists().find { it.id == listId }
    }

    fun updateList(updatedList: UserList) {
        val currentLists = getLists().toMutableList()
        val index = currentLists.indexOfFirst { it.id == updatedList.id }
        if (index != -1) {
            currentLists[index] = updatedList
            saveListsToStorage(currentLists)
        }
    }

    fun deleteList(listId: String) {
        val currentLists = getLists().toMutableList()
        currentLists.removeAll { it.id == listId }
        saveListsToStorage(currentLists)
    }

    fun addAlbumToList(listId: String, album: Music) {
        val list = getListById(listId)
        list?.let { currentList ->
            val updatedAlbums = currentList.albums.toMutableList()
            if (!updatedAlbums.any { it.id == album.id }) {
                updatedAlbums.add(album)
                val updatedList = currentList.copy(albums = updatedAlbums)
                updateList(updatedList)
            }
        }
    }

    fun removeAlbumFromList(listId: String, albumId: String) {
        val list = getListById(listId)
        list?.let { currentList ->
            val updatedAlbums = currentList.albums.toMutableList()
            updatedAlbums.removeAll { it.id == albumId }
            val updatedList = currentList.copy(albums = updatedAlbums)
            updateList(updatedList)
        }
    }

    fun calculateListCompletion(list: UserList): Int {
        // For now, return 0% - you can implement this based on user listening data
        return 0
    }

    fun generateListId(): String {
        return "list_${System.currentTimeMillis()}"
    }

    fun getCurrentTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun saveListsToStorage(lists: List<UserList>) {
        val jsonString = convertListsToJson(lists)
        sharedPreferences.edit().putString(key, jsonString).apply()
    }

    private fun convertListsToJson(lists: List<UserList>): String {
        val jsonArray = JSONArray()
        lists.forEach { list ->
            val jsonObject = JSONObject().apply {
                put("id", list.id)
                put("name", list.name)
                put("description", list.description)
                put("tags", JSONArray(list.tags))
                put("createdAt", list.createdAt)
                put("isPublic", list.isPublic)

                // Serialize albums
                val albumsArray = JSONArray()
                list.albums.forEach { album ->
                    val albumObj = JSONObject().apply {
                        put("id", album.id)
                        put("title", album.title)
                        put("artist", album.artist)
                        put("releaseYear", album.releaseYear)
                        put("coverImage", album.coverImage)
                        put("averageRating", album.averageRating)
                    }
                    albumsArray.put(albumObj)
                }
                put("albums", albumsArray)
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    private fun parseListsFromJson(jsonString: String): List<UserList> {
        val lists = mutableListOf<UserList>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)

                // Parse tags
                val tagsArray = jsonObject.getJSONArray("tags")
                val tags = mutableListOf<String>()
                for (j in 0 until tagsArray.length()) {
                    tags.add(tagsArray.getString(j))
                }

                // Parse albums
                val albumsArray = jsonObject.getJSONArray("albums")
                val albums = mutableListOf<Music>()
                for (j in 0 until albumsArray.length()) {
                    val albumObj = albumsArray.getJSONObject(j)
                    val album = Music(
                        id = albumObj.getString("id"),
                        title = albumObj.getString("title"),
                        artist = albumObj.getString("artist"),
                        releaseYear = albumObj.getInt("releaseYear"),
                        coverImage = albumObj.getString("coverImage"),
                        averageRating = albumObj.getDouble("averageRating"),
                        album = albumObj.optString("album", ""), // Add album parameter
                        genre = albumObj.optString("genre", "")  // Add genre parameter
                    )
                    albums.add(album)
                }
                val list = UserList(
                    id = jsonObject.getString("id"),
                    name = jsonObject.getString("name"),
                    description = jsonObject.getString("description"),
                    albums = albums, // This ensures albums are properly set
                    tags = tags,
                    createdAt = jsonObject.getString("createdAt"),
                    isPublic = jsonObject.getBoolean("isPublic")
                )
                lists.add(list)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return lists
    }
}