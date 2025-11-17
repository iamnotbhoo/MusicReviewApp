package student.projects.musicreviewapp.auth

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import student.projects.musicreviewapp.models.UserList
import student.projects.musicreviewapp.models.Music

class ListManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_lists", Context.MODE_PRIVATE)
    private val key = "user_lists_data"
    private val gson = Gson()

    fun createList(userList: UserList) {
        val currentLists = getLists().toMutableList()

        // Check if list already exists
        val existingIndex = currentLists.indexOfFirst { it.id == userList.id }
        if (existingIndex != -1) {
            // Update existing list
            currentLists[existingIndex] = userList
        } else {
            // Add new list
            currentLists.add(userList)
        }

        saveListsToStorage(currentLists)
    }

    // Add this method to your ListManager class
    fun addAlbumToList(listId: String, album: Music) {
        val lists = getLists().toMutableList()
        val listIndex = lists.indexOfFirst { it.id == listId }

        if (listIndex != -1) {
            val userList = lists[listIndex]

            // Check if album is already in the list
            if (!userList.albums.any { it.id == album.id }) {
                val updatedAlbums = userList.albums.toMutableList().apply {
                    add(album)
                }

                val updatedList = userList.copy(albums = updatedAlbums)
                lists[listIndex] = updatedList
                saveListsToStorage(lists)
            }
        }
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

    fun deleteList(listId: String) {
        val currentLists = getLists().toMutableList()
        currentLists.removeAll { it.id == listId }
        saveListsToStorage(currentLists)
    }

    fun getPopularLists(): List<UserList> {
        return getLists().sortedByDescending { it.likes }
    }

    fun likeList(listId: String) {
        val lists = getLists().toMutableList()
        val listIndex = lists.indexOfFirst { it.id == listId }
        if (listIndex != -1) {
            val userList = lists[listIndex]
            val updatedList = userList.copy(
                liked = true,
                likes = userList.likes + 1
            )
            lists[listIndex] = updatedList
            saveListsToStorage(lists)
        }
    }

    fun unlikeList(listId: String) {
        val lists = getLists().toMutableList()
        val listIndex = lists.indexOfFirst { it.id == listId }
        if (listIndex != -1) {
            val userList = lists[listIndex]
            val updatedList = userList.copy(
                liked = false,
                likes = maxOf(0, userList.likes - 1)
            )
            lists[listIndex] = updatedList
            saveListsToStorage(lists)
        }
    }

    fun getListsByCreator(creator: String): List<UserList> {
        return getLists().filter { it.creator == creator }
    }

    fun generateListId(): String {
        return "list_${System.currentTimeMillis()}"
    }

    fun getCurrentTimestamp(): String {
        return System.currentTimeMillis().toString()
    }

    private fun saveListsToStorage(lists: List<UserList>) {
        val jsonString = convertListsToJson(lists)
        sharedPreferences.edit().putString(key, jsonString).apply()
    }

    private fun convertListsToJson(lists: List<UserList>): String {
        val jsonArray = JSONArray()
        lists.forEach { userList ->
            val jsonObject = JSONObject().apply {
                put("id", userList.id)
                put("name", userList.name)
                put("description", userList.description)
                put("albums", JSONArray(userList.albums.map { album ->
                    JSONObject().apply {
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
                }))
                put("tags", JSONArray(userList.tags))
                put("createdAt", userList.createdAt)
                put("isPublic", userList.isPublic)
                put("creator", userList.creator)
                put("likes", userList.likes)
                put("liked", userList.liked) // Add liked field
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

                // Parse albums
                val albumsArray = jsonObject.getJSONArray("albums")
                val albums = mutableListOf<Music>()
                for (j in 0 until albumsArray.length()) {
                    val albumObject = albumsArray.getJSONObject(j)
                    val album = Music(
                        id = albumObject.getString("id"),
                        title = albumObject.getString("title"),
                        artist = albumObject.getString("artist"),
                        album = albumObject.getString("album"),
                        releaseYear = albumObject.getInt("releaseYear"),
                        genre = albumObject.getString("genre"),
                        coverImage = albumObject.getString("coverImage"),
                        averageRating = albumObject.getDouble("averageRating"),
                        reviewCount = albumObject.getInt("reviewCount")
                    )
                    albums.add(album)
                }

                // Parse tags
                val tagsArray = jsonObject.getJSONArray("tags")
                val tags = mutableListOf<String>()
                for (j in 0 until tagsArray.length()) {
                    tags.add(tagsArray.getString(j))
                }

                val userList = UserList(
                    id = jsonObject.getString("id"),
                    name = jsonObject.getString("name"),
                    description = jsonObject.getString("description"),
                    albums = albums,
                    tags = tags,
                    createdAt = jsonObject.getString("createdAt"),
                    isPublic = jsonObject.getBoolean("isPublic"),
                    creator = jsonObject.getString("creator"),
                    likes = jsonObject.optInt("likes", 0),
                    liked = jsonObject.optBoolean("liked", false) // Read liked field
                )
                lists.add(userList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return lists
    }
}