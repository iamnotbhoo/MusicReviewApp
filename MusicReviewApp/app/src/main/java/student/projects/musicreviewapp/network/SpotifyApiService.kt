package student.projects.musicreviewapp.network

import android.content.Context
import android.util.Base64
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import student.projects.musicreviewapp.models.Music
import student.projects.musicreviewapp.models.AlbumDetails
import java.util.Calendar

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

    // SEARCH METHOD - ADDED
    fun searchMusic(query: String, callback: SpotifyCallback<List<Music>>) {
        val token = context.getSharedPreferences("SpotifyPref", Context.MODE_PRIVATE)
            .getString("token", "") ?: ""

        if (token.isEmpty()) {
            generateToken(object : SpotifyCallback<String> {
                override fun onSuccess(result: String) {
                    searchMusicWithToken(query, result, callback)
                }
                override fun onError(error: String) {
                    callback.onError(error)
                }
            })
            return
        }

        searchMusicWithToken(query, token, callback)
    }

    private fun searchMusicWithToken(
        query: String,
        token: String,
        callback: SpotifyCallback<List<Music>>
    ) {
        // Search for albums and tracks
        val url = "$BASE_URL/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}&type=album,track&limit=20"

        val request = object : JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val musicList = mutableListOf<Music>()

                    // Parse albums first (priority)
                    if (response.has("albums")) {
                        val albums = response.getJSONObject("albums")
                        val items = albums.getJSONArray("items")
                        musicList.addAll(parseAlbumResults(items))
                    }

                    // Parse tracks second
                    if (response.has("tracks")) {
                        val tracks = response.getJSONObject("tracks")
                        val items = tracks.getJSONArray("items")
                        musicList.addAll(parseTrackResults(items))
                    }

                    // Remove duplicates based on album name and artist
                    val uniqueResults = musicList.distinctBy { "${it.album}-${it.artist}" }
                    callback.onSuccess(uniqueResults)

                    Log.d("SpotifySearch", "Search completed: ${uniqueResults.size} results")
                } catch (e: JSONException) {
                    callback.onError("Failed to parse search results: ${e.message}")
                }
            },
            { error ->
                val code = error.networkResponse?.statusCode
                if (code == 401) {
                    // Token expired, regenerate and retry
                    generateToken(object : SpotifyCallback<String> {
                        override fun onSuccess(result: String) {
                            searchMusicWithToken(query, result, callback)
                        }
                        override fun onError(errorMsg: String) {
                            callback.onError(errorMsg)
                        }
                    })
                } else {
                    callback.onError("Failed to search: ${error.message}")
                }
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

    private fun parseAlbumResults(items: JSONArray): List<Music> {
        val musicList = mutableListOf<Music>()

        for (i in 0 until items.length()) {
            try {
                val albumObj = items.getJSONObject(i)
                val albumName = albumObj.getString("name")
                val albumId = albumObj.getString("id")

                val artists = albumObj.getJSONArray("artists")
                val artistName = if (artists.length() > 0)
                    artists.getJSONObject(0).getString("name")
                else "Unknown Artist"

                // Get album art - try multiple image sizes
                val images = albumObj.getJSONArray("images")
                var imageUrl = ""
                if (images.length() > 0) {
                    // Prefer medium size image (index 1), fallback to large (index 0)
                    when {
                        images.length() > 1 -> imageUrl = images.getJSONObject(1).optString("url", "")
                        images.length() > 0 -> imageUrl = images.getJSONObject(0).optString("url", "")
                    }
                }

                val releaseDate = albumObj.optString("release_date", "")
                val releaseYear = try {
                    releaseDate.split("-")[0].toInt()
                } catch (e: Exception) {
                    Calendar.getInstance().get(Calendar.YEAR)
                }

                val popularity = albumObj.optInt("popularity", 0)
                val totalTracks = albumObj.optInt("total_tracks", 0)

                musicList.add(Music(
                    id = albumId,
                    title = albumName, // Album name as title
                    artist = artistName,
                    album = albumName,
                    releaseYear = releaseYear,
                    genre = "Various",
                    coverImage = imageUrl,
                    averageRating = (popularity / 20.0).coerceAtMost(5.0),
                    reviewCount = totalTracks * 2
                ))

                Log.d("SpotifySearch", "Album: $albumName by $artistName - Image: $imageUrl")
            } catch (e: Exception) {
                Log.e("SpotifyApi", "Error parsing album: ${e.message}")
            }
        }
        return musicList
    }

    private fun parseTrackResults(items: JSONArray): List<Music> {
        val musicList = mutableListOf<Music>()

        for (i in 0 until items.length()) {
            try {
                val item = items.getJSONObject(i)
                val trackName = item.getString("name")
                val trackId = item.getString("id")

                val artists = item.getJSONArray("artists")
                val artistName = if (artists.length() > 0)
                    artists.getJSONObject(0).getString("name")
                else "Unknown Artist"

                val albumObj = item.getJSONObject("album")
                val albumName = albumObj.getString("name")

                // Get album art from the track's album
                val images = albumObj.getJSONArray("images")
                var imageUrl = ""
                if (images.length() > 0) {
                    when {
                        images.length() > 1 -> imageUrl = images.getJSONObject(1).optString("url", "")
                        images.length() > 0 -> imageUrl = images.getJSONObject(0).optString("url", "")
                    }
                }

                val releaseDate = albumObj.optString("release_date", "")
                val releaseYear = try {
                    releaseDate.split("-")[0].toInt()
                } catch (e: Exception) {
                    Calendar.getInstance().get(Calendar.YEAR)
                }

                val popularity = item.optInt("popularity", 0)

                // For track results, show the album name as title
                musicList.add(Music(
                    id = trackId,
                    title = albumName, // Use album name instead of track name
                    artist = artistName,
                    album = albumName,
                    releaseYear = releaseYear,
                    genre = "Various",
                    coverImage = imageUrl,
                    averageRating = (popularity / 20.0).coerceAtMost(5.0),
                    reviewCount = (popularity / 5)
                ))

                Log.d("SpotifySearch", "Track Album: $albumName by $artistName - Image: $imageUrl")
            } catch (e: Exception) {
                Log.e("SpotifyApi", "Error parsing track: ${e.message}")
            }
        }
        return musicList
    }

    // EXISTING METHODS - KEEP THESE
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

    fun getNewReleases(callback: SpotifyCallback<List<Music>>) {
        // Get brand new album releases
        val url = "$BASE_URL/browse/new-releases?country=US&limit=8"
        makeApiCall(url, callback) { response ->
            parseNewReleasesResponse(response)
        }
    }

    fun getFeaturedPlaylists(callback: SpotifyCallback<List<Music>>) {
        // Get Spotify's featured playlists (often contain current popular music)
        val url = "$BASE_URL/browse/featured-playlists?country=US&limit=8"
        makeApiCall(url, callback) { response ->
            parseFeaturedPlaylistsResponse(response)
        }
    }

    fun getTopTracks(callback: SpotifyCallback<List<Music>>) {
        // Get current top tracks globally
        val url = "$BASE_URL/playlists/37i9dQZEVXbMDoHDwVN2tF/tracks?limit=8" // Global Top 50 playlist
        makeApiCall(url, callback) { response ->
            parseTopTracksResponse(response)
        }
    }

    private fun makeApiCall(
        url: String,
        callback: SpotifyCallback<List<Music>>,
        parseFunction: (JSONObject) -> List<Music>
    ) {
        val token = context.getSharedPreferences("SpotifyPref", Context.MODE_PRIVATE)
            .getString("token", "") ?: ""

        if (token.isEmpty()) {
            generateToken(object : SpotifyCallback<String> {
                override fun onSuccess(result: String) {
                    makeApiCallWithToken(url, result, callback, parseFunction)
                }
                override fun onError(error: String) {
                    callback.onError(error)
                }
            })
            return
        }

        makeApiCallWithToken(url, token, callback, parseFunction)
    }

    private fun makeApiCallWithToken(
        url: String,
        token: String,
        callback: SpotifyCallback<List<Music>>,
        parseFunction: (JSONObject) -> List<Music>
    ) {
        val request = object : JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val musicList = parseFunction(response)
                    callback.onSuccess(musicList)
                } catch (e: Exception) {
                    callback.onError("Failed to parse response: ${e.message}")
                }
            },
            { error ->
                val code = error.networkResponse?.statusCode

                if (code == 401) {
                    Log.w("Spotify", "Token expired → regenerating")

                    generateToken(object : SpotifyCallback<String> {
                        override fun onSuccess(result: String) {
                            // Retry request with fresh token
                            makeApiCallWithToken(url, result, callback, parseFunction)
                        }
                        override fun onError(errorMsg: String) {
                            callback.onError(errorMsg)
                        }
                    })
                } else {
                    callback.onError("Failed to load data: ${error.message}")
                }
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

    private fun parseNewReleasesResponse(response: JSONObject): List<Music> {
        val musicList = mutableListOf<Music>()
        try {
            val albums = response.getJSONObject("albums")
            val items = albums.getJSONArray("items")

            for (i in 0 until items.length()) {
                val album = items.getJSONObject(i)
                musicList.add(createMusicFromAlbum(album))
            }
        } catch (e: JSONException) {
            Log.e("SpotifyApi", "Error parsing new releases: ${e.message}")
        }
        return musicList
    }

    private fun parseFeaturedPlaylistsResponse(response: JSONObject): List<Music> {
        val musicList = mutableListOf<Music>()
        try {
            val playlists = response.getJSONObject("playlists")
            val items = playlists.getJSONArray("items")

            for (i in 0 until items.length()) {
                val playlist = items.getJSONObject(i)
                // For playlists, we'll use the playlist as "album" and curator as "artist"
                val images = playlist.getJSONArray("images")
                val imageUrl = if (images.length() > 0)
                    images.getJSONObject(0).optString("url", "")
                else ""

                musicList.add(Music(
                    id = playlist.optString("id", ""),
                    title = playlist.optString("name", "Unknown Playlist"),
                    artist = "Spotify Curated",
                    album = playlist.optString("name", "Unknown Playlist"),
                    releaseYear = Calendar.getInstance().get(Calendar.YEAR),
                    genre = "Various",
                    coverImage = imageUrl,
                    averageRating = 4.0 + (i % 3) * 0.3, // Random rating between 4.0-4.6
                    reviewCount = (i + 1) * 15
                ))
            }
        } catch (e: JSONException) {
            Log.e("SpotifyApi", "Error parsing featured playlists: ${e.message}")
        }
        return musicList
    }

    private fun parseTopTracksResponse(response: JSONObject): List<Music> {
        val musicList = mutableListOf<Music>()
        try {
            val items = response.getJSONArray("items")

            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                val track = item.getJSONObject("track")
                val album = track.getJSONObject("album")

                val artists = track.getJSONArray("artists")
                val artistName = if (artists.length() > 0)
                    artists.getJSONObject(0).optString("name", "Unknown Artist")
                else "Unknown Artist"

                val images = album.getJSONArray("images")
                val imageUrl = if (images.length() > 0)
                    images.getJSONObject(0).optString("url", "")
                else ""

                val releaseDate = album.optString("release_date", "")
                val releaseYear = try {
                    releaseDate.split("-")[0].toInt()
                } catch (e: Exception) {
                    Calendar.getInstance().get(Calendar.YEAR)
                }

                musicList.add(Music(
                    id = track.optString("id", ""),
                    title = track.optString("name", "Unknown Track"),
                    artist = artistName,
                    album = album.optString("name", "Unknown Album"),
                    releaseYear = releaseYear,
                    genre = "Popular",
                    coverImage = imageUrl,
                    averageRating = 4.2 + (i % 4) * 0.2, // Random rating between 4.2-4.8
                    reviewCount = (i + 1) * 20
                ))
            }
        } catch (e: JSONException) {
            Log.e("SpotifyApi", "Error parsing top tracks: ${e.message}")
        }
        return musicList
    }

    private fun createMusicFromAlbum(album: JSONObject): Music {
        val artists = album.getJSONArray("artists")
        val artistName = if (artists.length() > 0)
            artists.getJSONObject(0).optString("name", "Unknown Artist")
        else "Unknown Artist"

        val images = album.getJSONArray("images")
        val imageUrl = if (images.length() > 0)
            images.getJSONObject(0).optString("url", "")
        else ""

        val releaseDate = album.optString("release_date", "")
        val releaseYear = try {
            releaseDate.split("-")[0].toInt()
        } catch (e: Exception) {
            Calendar.getInstance().get(Calendar.YEAR)
        }

        return Music(
            id = album.optString("id", ""),
            title = album.optString("name", "Unknown Album"),
            artist = artistName,
            album = album.optString("name", "Unknown Album"),
            releaseYear = releaseYear,
            genre = "New Release",
            coverImage = imageUrl,
            averageRating = 4.0 + (Math.random() * 1.0), // Random rating between 4.0-5.0
            reviewCount = (Math.random() * 100 + 50).toInt() // Random review count 50-150
        )
    }

    // Updated method names to reflect what they actually fetch
    fun getRecommendedAlbums(callback: SpotifyCallback<List<Music>>) {
        getNewReleases(callback) // Use new releases for recommended
    }

    fun getPopularAlbums(callback: SpotifyCallback<List<Music>>) {
        getTopTracks(callback) // Use top tracks for popular
    }

    fun getTrendingAlbums(callback: SpotifyCallback<List<Music>>) {
        getFeaturedPlaylists(callback) // Use featured playlists for trending
    }

    // Add this method to get detailed album information
    fun getAlbumDetails(albumId: String, callback: SpotifyCallback<AlbumDetails>) {
        val token = context.getSharedPreferences("SpotifyPref", Context.MODE_PRIVATE)
            .getString("token", "") ?: ""

        if (token.isEmpty()) {
            generateToken(object : SpotifyCallback<String> {
                override fun onSuccess(result: String) {
                    getAlbumDetailsWithToken(albumId, result, callback)
                }
                override fun onError(error: String) {
                    callback.onError(error)
                }
            })
            return
        }

        getAlbumDetailsWithToken(albumId, token, callback)
    }

    private fun getAlbumDetailsWithToken(
        albumId: String,
        token: String,
        callback: SpotifyCallback<AlbumDetails>
    ) {
        val url = "$BASE_URL/albums/$albumId"

        val request = object : JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val albumDetails = parseAlbumDetails(response)
                    callback.onSuccess(albumDetails)
                } catch (e: JSONException) {
                    callback.onError("Failed to parse album details: ${e.message}")
                }
            },
            { error ->
                val code = error.networkResponse?.statusCode
                if (code == 401) {
                    generateToken(object : SpotifyCallback<String> {
                        override fun onSuccess(result: String) {
                            getAlbumDetailsWithToken(albumId, result, callback)
                        }
                        override fun onError(errorMsg: String) {
                            callback.onError(errorMsg)
                        }
                    })
                } else {
                    callback.onError("Failed to get album details: ${error.message}")
                }
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

    private fun parseAlbumDetails(response: JSONObject): AlbumDetails {
        val albumName = response.getString("name")
        val albumId = response.getString("id")

        // Parse artists
        val artistsArray = response.getJSONArray("artists")
        val artists = mutableListOf<String>()
        for (i in 0 until artistsArray.length()) {
            artists.add(artistsArray.getJSONObject(i).getString("name"))
        }
        val artistName = artists.joinToString(", ")

        // Parse images
        val imagesArray = response.getJSONArray("images")
        var imageUrl = ""
        var largeImageUrl = ""
        if (imagesArray.length() > 0) {
            imageUrl = imagesArray.getJSONObject(0).getString("url") // Largest image
            if (imagesArray.length() > 1) {
                largeImageUrl = imagesArray.getJSONObject(1).getString("url") // Medium image
            }
        }

        // Parse release date and format it
        val releaseDate = response.getString("release_date")
        val releaseDatePrecision = response.getString("release_date_precision")
        val formattedReleaseDate = formatReleaseDate(releaseDate, releaseDatePrecision)

        // Parse total duration
        val tracksArray = response.getJSONObject("tracks").getJSONArray("items")
        var totalDurationMs = 0L
        for (i in 0 until tracksArray.length()) {
            totalDurationMs += tracksArray.getJSONObject(i).getLong("duration_ms")
        }
        val totalDuration = formatDuration(totalDurationMs)
        val totalTracks = tracksArray.length()

        // Parse genres
        val genresArray = response.getJSONArray("genres")
        val genres = mutableListOf<String>()
        for (i in 0 until genresArray.length()) {
            genres.add(genresArray.getString(i))
        }

        // Parse label and copyright
        val label = response.optString("label", "Unknown Label")
        val copyrightsArray = response.getJSONArray("copyrights")
        val copyright = if (copyrightsArray.length() > 0) {
            copyrightsArray.getJSONObject(0).getString("text")
        } else {
            ""
        }

        // Get album description/overview (from Spotify's external URLs or use a fallback)
        val externalUrls = response.getJSONObject("external_urls")
        val spotifyUrl = externalUrls.getString("spotify")

        return AlbumDetails(
            id = albumId,
            title = albumName,
            artist = artistName,
            artists = artists,
            releaseDate = releaseDate,
            formattedReleaseDate = formattedReleaseDate,
            releaseDatePrecision = releaseDatePrecision,
            totalTracks = totalTracks,
            totalDurationMs = totalDurationMs,
            formattedDuration = totalDuration,
            genres = genres,
            label = label,
            copyright = copyright,
            coverImage = imageUrl,
            largeCoverImage = largeImageUrl,
            spotifyUrl = spotifyUrl,
            popularity = response.optInt("popularity", 0)
        )
    }

    private fun formatReleaseDate(releaseDate: String, precision: String): String {
        return try {
            when (precision) {
                "year" -> {
                    // Just the year
                    releaseDate
                }
                "month" -> {
                    // Year and month (e.g., "2023-03" -> "March 2023")
                    val parts = releaseDate.split("-")
                    if (parts.size >= 2) {
                        val year = parts[0]
                        val month = when (parts[1]) {
                            "01" -> "January"
                            "02" -> "February"
                            "03" -> "March"
                            "04" -> "April"
                            "05" -> "May"
                            "06" -> "June"
                            "07" -> "July"
                            "08" -> "August"
                            "09" -> "September"
                            "10" -> "October"
                            "11" -> "November"
                            "12" -> "December"
                            else -> parts[1]
                        }
                        "$month $year"
                    } else {
                        releaseDate
                    }
                }
                "day" -> {
                    // Full date (e.g., "2023-03-15" -> "March 15, 2023")
                    val parts = releaseDate.split("-")
                    if (parts.size >= 3) {
                        val year = parts[0]
                        val month = when (parts[1]) {
                            "01" -> "January"
                            "02" -> "February"
                            "03" -> "March"
                            "04" -> "April"
                            "05" -> "May"
                            "06" -> "June"
                            "07" -> "July"
                            "08" -> "August"
                            "09" -> "September"
                            "10" -> "October"
                            "11" -> "November"
                            "12" -> "December"
                            else -> parts[1]
                        }
                        val day = parts[2].toInt() // Remove leading zeros
                        "$month $day, $year"
                    } else {
                        releaseDate
                    }
                }
                else -> releaseDate
            }
        } catch (e: Exception) {
            releaseDate
        }
    }

    private fun formatDuration(totalDurationMs: Long): String {
        val totalSeconds = totalDurationMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60

        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }
}