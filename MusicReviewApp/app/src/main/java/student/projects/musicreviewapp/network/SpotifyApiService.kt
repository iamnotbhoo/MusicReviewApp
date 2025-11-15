package student.projects.musicreviewapp.network

import android.content.Context
import android.util.Base64
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import student.projects.musicreviewapp.models.Music

class SpotifyApiService(private val context: Context) {

    private val queue = Volley.newRequestQueue(context)
    private var isTokenGenerated = false

    companion object {
        private const val CLIENT_ID = "4c8097e74f224172afd3a98041406808"
        private const val CLIENT_SECRET = "722297a8aa5842b2874e48307f23f92d"
        private const val TOKEN_URL = "https://accounts.spotify.com/api/token"
        private const val BASE_URL = "https://api.spotify.com/v1"
    }

    interface SpotifyCallback<T> {
        fun onSuccess(result: T)
        fun onError(error: String)
    }

    private fun generateToken(callback: SpotifyCallback<String>) {
        val request = object : StringRequest(
            Request.Method.POST, TOKEN_URL,
            { response ->
                try {
                    val token = JSONObject(response).getString("access_token")
                    val sharedPref = context.getSharedPreferences("SpotifyPref", Context.MODE_PRIVATE)
                    sharedPref.edit().putString("token", "Bearer $token").apply()
                    isTokenGenerated = true
                    callback.onSuccess("Bearer $token")
                } catch (e: JSONException) {
                    callback.onError("Failed to parse token response: ${e.message}")
                }
            },
            { error -> callback.onError("Failed to get token: ${error.message}") }
        ) {
            override fun getHeaders(): Map<String, String> {
                val credentials = "$CLIENT_ID:$CLIENT_SECRET"
                val auth = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
                return mapOf(
                    "Authorization" to "Basic $auth",
                    "Content-Type" to "application/x-www-form-urlencoded"
                )
            }

            override fun getParams(): MutableMap<String, String> {
                return mutableMapOf("grant_type" to "client_credentials")
            }
        }
        queue.add(request)
    }

    fun getRecommendedAlbums(callback: SpotifyCallback<List<Music>>) {
        val albumIds = listOf(
            "5Nwsra93UQYJ6xxcjcE10x", "0z7bJ6UpjUw8U4TATtc5Ku",
            "36UJ90D0e295TvlU109Xvy", "3uuu6u13U0KeVQsZ3CZKK4",
            "45ZIondgVoMB84MQQaUo9T", "15CyNDuGY5fsG0Hn9rjnpG",
            "1HeX4SmCFW4EPHQDvHgrVS", "6mCDTT1XGTf48p6FkK9qFL"
        )
        loadAlbums(albumIds, callback)
    }

    fun getPopularAlbums(callback: SpotifyCallback<List<Music>>) {
        val albumIds = listOf(
            "0sjyZypccO1vyihqaAkdt3", "17vZRWjKOX7TmMktjQL2Qx",
            "5Nwsra93UQYJ6xxcjcE10x", "2zXKlf81VmDHIMtQe3oD0r",
            "7Gws1vUsWltRs58x8QuYVQ", "7uftfPn8f7lwtRLUrEVRYM",
            "7kSY0fqrPep5vcwOb1juye"
        )
        loadAlbums(albumIds, callback)
    }

    fun getTrendingAlbums(callback: SpotifyCallback<List<Music>>) {
        val albumIds = listOf(
            "1P4eCx5b11Tfmi4s1GmWmQ", "2SsEtiB6yJYn8hRRAmtVda",
            "7hhxms8KCwlQCWffIJpN9b", "3umvKIjsD484pa9pCyPK2x",
            "3OHC6XD29wXWADtAOP2geV", "3RZxrS2dDZlbsYtMRM89v8",
            "24C47633GRlozws7WBth7t"
        )
        loadAlbums(albumIds, callback)
    }

    private fun loadAlbums(albumIds: List<String>, callback: SpotifyCallback<List<Music>>) {
        val token = context.getSharedPreferences("SpotifyPref", Context.MODE_PRIVATE)
            .getString("token", "") ?: ""

        if (token.isEmpty()) {
            generateToken(object : SpotifyCallback<String> {
                override fun onSuccess(result: String) {
                    loadAlbumsWithToken(albumIds, result, callback)
                }
                override fun onError(error: String) {
                    callback.onError(error)
                }
            })
            return
        }

        loadAlbumsWithToken(albumIds, token, callback)
    }

    private fun loadAlbumsWithToken(
        albumIds: List<String>,
        token: String,
        callback: SpotifyCallback<List<Music>>
    ) {
        val url = "$BASE_URL/albums?ids=${albumIds.joinToString(",")}"

        val request = object : JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    parseAlbumResponse(response, callback)
                } catch (e: Exception) {
                    callback.onError("Failed to parse album data: ${e.message}")
                }
            },
            { error ->
                callback.onError("Failed to load albums: ${error.message}")
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                return mapOf(
                    "Authorization" to token,
                    "Accept" to "application/json",
                    "Content-Type" to "application/json"
                )
            }
        }
        queue.add(request)
    }

    private fun parseAlbumResponse(response: JSONObject, callback: SpotifyCallback<List<Music>>) {
        try {
            val albumArray = response.getJSONArray("albums")
            val albumList = mutableListOf<Music>()

            for (i in 0 until albumArray.length()) {
                val albumObj = albumArray.getJSONObject(i)
                val artists = albumObj.getJSONArray("artists")
                val images = albumObj.getJSONArray("images")

                val artistName = if (artists.length() > 0)
                    artists.getJSONObject(0).optString("name", "Unknown Artist")
                else "Unknown Artist"

                val imageUrl = if (images.length() > 1)
                    images.getJSONObject(1).optString("url", "")
                else ""

                val releaseDate = albumObj.optString("release_date", "")
                val releaseYear = try {
                    releaseDate.split("-")[0].toInt()
                } catch (e: Exception) {
                    2023
                }

                albumList.add(Music(
                    id = albumObj.optString("id", ""),
                    title = albumObj.optString("name", "Unknown Album"),
                    artist = artistName,
                    album = albumObj.optString("name", "Unknown Album"),
                    releaseYear = releaseYear,
                    genre = "Various", // Spotify doesn't provide genre in album search
                    coverImage = imageUrl,
                    averageRating = (3.5 + (i % 5) * 0.3).coerceAtMost(5.0),
                    reviewCount = (i + 1) * 10
                ))
            }

            callback.onSuccess(albumList)
        } catch (e: JSONException) {
            callback.onError("JSON parsing error: ${e.message}")
        } catch (e: Exception) {
            callback.onError("Error parsing album data: ${e.message}")
        }
    }

    fun searchTracks(query: String, callback: SpotifyCallback<List<Music>>) {
        val token = context.getSharedPreferences("SpotifyPref", Context.MODE_PRIVATE)
            .getString("token", "") ?: ""

        if (token.isEmpty()) {
            generateToken(object : SpotifyCallback<String> {
                override fun onSuccess(result: String) {
                    searchWithToken(query, result, callback)
                }
                override fun onError(error: String) {
                    callback.onError(error)
                }
            })
            return
        }

        searchWithToken(query, token, callback)
    }

    private fun searchWithToken(query: String, token: String, callback: SpotifyCallback<List<Music>>) {
        val url = "$BASE_URL/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}&type=track"

        val request = object : JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val trackObj = response.getJSONObject("tracks")
                    val itemsArray = trackObj.getJSONArray("items")
                    val musicList = mutableListOf<Music>()

                    for (i in 0 until itemsArray.length()) {
                        val itemObj = itemsArray.getJSONObject(i)
                        val trackName = itemObj.getString("name")
                        val trackArtist = itemObj.getJSONArray("artists")
                            .getJSONObject(0).getString("name")
                        val trackId = itemObj.getString("id")

                        val albumObj = itemObj.getJSONObject("album")
                        val images = albumObj.getJSONArray("images")
                        val imageUrl = if (images.length() > 1)
                            images.getJSONObject(1).optString("url", "")
                        else ""

                        val releaseDate = albumObj.optString("release_date", "")
                        val releaseYear = try {
                            releaseDate.split("-")[0].toInt()
                        } catch (e: Exception) {
                            2023
                        }

                        musicList.add(Music(
                            id = trackId,
                            title = trackName,
                            artist = trackArtist,
                            album = albumObj.optString("name", "Unknown Album"),
                            releaseYear = releaseYear,
                            genre = "Various",
                            coverImage = imageUrl,
                            averageRating = 4.0,
                            reviewCount = 0
                        ))
                    }

                    callback.onSuccess(musicList)
                } catch (e: JSONException) {
                    callback.onError("Failed to parse search results: ${e.message}")
                }
            },
            { error -> callback.onError("Failed to search: ${error.message}") }
        ) {
            override fun getHeaders(): Map<String, String> {
                return mapOf(
                    "Authorization" to token,
                    "Accept" to "application/json",
                    "Content-Type" to "application/json"
                )
            }
        }
        queue.add(request)
    }
}