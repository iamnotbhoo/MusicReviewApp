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

    // Token management variables
    private var currentToken: String? = null
    private var tokenExpiryTime: Long = 0
    private val TOKEN_EXPIRY_BUFFER = 5 * 60 * 1000 // 5 minutes buffer

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

    // ========== TOKEN MANAGEMENT ==========

    private fun getValidToken(callback: SpotifyCallback<String>) {
        val sharedPref = context.getSharedPreferences("SpotifyPref", Context.MODE_PRIVATE)
        val savedToken = sharedPref.getString("token", null)
        val currentTime = System.currentTimeMillis()

        // Check if we have a valid token that's not expired
        if (savedToken != null && currentTime < tokenExpiryTime) {
            Log.d("SpotifyAPI", "✅ Using cached token (expires in ${(tokenExpiryTime - currentTime) / 1000}s)")
            callback.onSuccess(savedToken)
            return
        }

        // Token is expired or doesn't exist, generate a new one
        Log.d("SpotifyAPI", "🔄 Token expired or missing, generating new token...")
        generateToken(object : SpotifyCallback<String> {
            override fun onSuccess(result: String) {
                currentToken = result
                tokenExpiryTime = System.currentTimeMillis() + 3600000 // 1 hour from now
                sharedPref.edit().putString("token", result).apply()
                Log.d("SpotifyAPI", "✅ New token generated, expires at ${tokenExpiryTime}")
                callback.onSuccess(result)
            }

            override fun onError(error: String) {
                Log.e("SpotifyAPI", "❌ Failed to generate token: $error")
                callback.onError(error)
            }
        })
    }

    private fun generateToken(callback: SpotifyCallback<String>) {
        val request = object : StringRequest(
            Request.Method.POST, TOKEN_URL,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val token = jsonResponse.getString("access_token")
                    val tokenType = jsonResponse.getString("token_type")
                    val expiresIn = jsonResponse.getInt("expires_in")

                    val fullToken = "$tokenType $token"
                    Log.d("SpotifyAPI", "🔑 Token generated successfully, expires in ${expiresIn}s")
                    callback.onSuccess(fullToken)
                } catch (e: JSONException) {
                    Log.e("SpotifyAPI", "❌ Failed to parse token response: ${e.message}")
                    callback.onError("Failed to parse token response: ${e.message}")
                }
            },
            { error ->
                val errorMsg = error.message ?: "Unknown error"
                Log.e("SpotifyAPI", "❌ Token request failed: $errorMsg")
                callback.onError("Failed to get token: $errorMsg")
            }
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

    fun clearSpotifyCache() {
        val sharedPref = context.getSharedPreferences("SpotifyPref", Context.MODE_PRIVATE)
        sharedPref.edit().remove("token").apply()
        currentToken = null
        tokenExpiryTime = 0
        Log.d("SpotifyAPI", "🧹 Cleared Spotify cache")
    }

    // ========== API METHODS WITH TOKEN MANAGEMENT ==========

    // SEARCH METHOD
    fun searchMusic(query: String, callback: SpotifyCallback<List<Music>>) {
        getValidToken(object : SpotifyCallback<String> {
            override fun onSuccess(token: String) {
                searchMusicWithToken(query, token, callback)
            }
            override fun onError(error: String) {
                callback.onError(error)
            }
        })
    }

    private fun searchMusicWithToken(
        query: String,
        token: String,
        callback: SpotifyCallback<List<Music>>
    ) {
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
                    Log.w("SpotifyAPI", "🔄 Token expired during search, regenerating...")
                    clearSpotifyCache()
                    searchMusic(query, callback) // Retry with new token
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

    fun getPopularThisWeek(callback: SpotifyCallback<List<Music>>) {
        val popularArtists2024 = listOf(
            "Travis Scott", "Drake", "The Weeknd", "Olivia Rodrigo",
            "SZA", "Harry Styles", "Doja Cat", "Ariana Grande",
            "Kanye West", "Beyoncé"
        )

        val selectedArtists = popularArtists2024.shuffled().take(4)
        Log.d("SpotifyAPI", "🔥 Searching for albums by: ${selectedArtists.joinToString(", ")}")

        searchAlbumsFromMultipleArtists(selectedArtists, callback)
    }

    private fun searchAlbumsFromMultipleArtists(artists: List<String>, callback: SpotifyCallback<List<Music>>) {
        getValidToken(object : SpotifyCallback<String> {
            override fun onSuccess(token: String) {
                searchAlbumsFromMultipleArtistsWithToken(artists, token, callback)
            }
            override fun onError(error: String) {
                callback.onError(error)
            }
        })
    }

    private fun searchAlbumsFromMultipleArtistsWithToken(
        artists: List<String>,
        token: String,
        callback: SpotifyCallback<List<Music>>
    ) {
        val allAlbums = mutableListOf<Music>()
        var completedSearches = 0
        var errors = 0

        fun checkCompletion() {
            completedSearches++
            if (completedSearches == artists.size) {
                if (allAlbums.isEmpty()) {
                    callback.onError("No albums found for the specified artists")
                } else {
                    val groupedByArtist = allAlbums.groupBy { it.artist }
                    val diverseAlbums = mutableListOf<Music>()

                    artists.forEach { artist ->
                        val artistAlbums = groupedByArtist[artist] ?: emptyList()
                        val bestAlbum = artistAlbums
                            .distinctBy { it.album }
                            .maxByOrNull { it.averageRating }
                            ?: artistAlbums.firstOrNull()

                        bestAlbum?.let { diverseAlbums.add(it) }
                    }

                    Log.d("SpotifyAPI", "🎵 Found ${diverseAlbums.size} albums from ${artists.size} artists")
                    callback.onSuccess(diverseAlbums.take(4))
                }
            }
        }

        artists.forEach { artist ->
            val url = "$BASE_URL/search?q=artist:${java.net.URLEncoder.encode(artist, "UTF-8")}&type=album&limit=5"

            val request = object : JsonObjectRequest(
                Request.Method.GET, url, null,
                { response ->
                    try {
                        if (response.has("albums")) {
                            val albums = response.getJSONObject("albums")
                            val items = albums.getJSONArray("items")

                            for (i in 0 until items.length()) {
                                val albumObj = items.getJSONObject(i)
                                val albumName = albumObj.getString("name")
                                val albumId = albumObj.getString("id")

                                val albumArtists = albumObj.getJSONArray("artists")
                                val mainArtist = if (albumArtists.length() > 0)
                                    albumArtists.getJSONObject(0).getString("name")
                                else "Unknown Artist"

                                if (mainArtist.equals(artist, ignoreCase = true)) {
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

                                    val popularity = albumObj.optInt("popularity", 0)

                                    allAlbums.add(Music(
                                        id = albumId,
                                        title = albumName,
                                        artist = mainArtist,
                                        album = albumName,
                                        releaseYear = releaseYear,
                                        genre = "Popular",
                                        coverImage = imageUrl,
                                        averageRating = (popularity / 20.0).coerceAtMost(5.0),
                                        reviewCount = popularity * 2
                                    ))

                                    Log.d("SpotifyAPI", "🎵 Found album: $albumName by $mainArtist")
                                }
                            }
                        }
                        checkCompletion()
                    } catch (e: JSONException) {
                        Log.e("SpotifyAPI", "Error parsing albums for $artist: ${e.message}")
                        errors++
                        checkCompletion()
                    }
                },
                { error ->
                    val code = error.networkResponse?.statusCode
                    if (code == 401) {
                        Log.w("SpotifyAPI", "🔄 Token expired during artist search, will retry...")
                        errors++
                        checkCompletion()
                    } else {
                        Log.e("SpotifyAPI", "Failed to search albums for $artist: ${error.message}")
                        errors++
                        checkCompletion()
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
    }

    fun getFriendsAlbums(callback: SpotifyCallback<List<Music>>) {
        val friendArtists = listOf(
            "Billie Eilish", "Kendrick Lamar", "Tyler The Creator", "Frank Ocean", "The Beatles",
            "Queen"
        )

        val selectedArtists = friendArtists.shuffled().take(4)
        Log.d("SpotifyAPI", "👥 Searching for friend albums by: ${selectedArtists.joinToString(", ")}")

        searchAlbumsFromMultipleArtists(selectedArtists, callback)
    }

    // Simple and reliable trending music fetcher
    fun getSimpleTrendingMusic(callback: SpotifyCallback<List<Music>>) {
        val popularTerms = listOf(
            "2024 hits",
            "popular now",
            "chart top",
            "viral songs",
            "trending music"
        )

        val randomTerm = popularTerms.random()
        Log.d("SpotifyAPI", "🎯 Searching for trending with term: $randomTerm")
        searchMusic(randomTerm, callback)
    }

    // Add this method to get detailed album information
    fun getAlbumDetails(albumId: String, callback: SpotifyCallback<AlbumDetails>) {
        getValidToken(object : SpotifyCallback<String> {
            override fun onSuccess(token: String) {
                getAlbumDetailsWithToken(albumId, token, callback)
            }
            override fun onError(error: String) {
                callback.onError(error)
            }
        })
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
                    Log.w("SpotifyAPI", "🔄 Token expired during album details, regenerating...")
                    clearSpotifyCache()
                    getAlbumDetails(albumId, callback) // Retry with new token
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

    // ========== EXISTING PARSING METHODS (keep these as they are) ==========

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

                val popularity = albumObj.optInt("popularity", 0)
                val totalTracks = albumObj.optInt("total_tracks", 0)

                musicList.add(Music(
                    id = albumId,
                    title = albumName,
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

                musicList.add(Music(
                    id = trackId,
                    title = albumName,
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
            imageUrl = imagesArray.getJSONObject(0).getString("url")
            if (imagesArray.length() > 1) {
                largeImageUrl = imagesArray.getJSONObject(1).getString("url")
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

        // Get album description/overview
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
                "year" -> releaseDate
                "month" -> {
                    val parts = releaseDate.split("-")
                    if (parts.size >= 2) {
                        val year = parts[0]
                        val month = when (parts[1]) {
                            "01" -> "January"; "02" -> "February"; "03" -> "March"; "04" -> "April"
                            "05" -> "May"; "06" -> "June"; "07" -> "July"; "08" -> "August"
                            "09" -> "September"; "10" -> "October"; "11" -> "November"; "12" -> "December"
                            else -> parts[1]
                        }
                        "$month $year"
                    } else releaseDate
                }
                "day" -> {
                    val parts = releaseDate.split("-")
                    if (parts.size >= 3) {
                        val year = parts[0]
                        val month = when (parts[1]) {
                            "01" -> "January"; "02" -> "February"; "03" -> "March"; "04" -> "April"
                            "05" -> "May"; "06" -> "June"; "07" -> "July"; "08" -> "August"
                            "09" -> "September"; "10" -> "October"; "11" -> "November"; "12" -> "December"
                            else -> parts[1]
                        }
                        val day = parts[2].toInt()
                        "$month $day, $year"
                    } else releaseDate
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
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }

    // ========== OTHER API METHODS (updated to use token management) ==========

    fun getNewReleases(callback: SpotifyCallback<List<Music>>) {
        makeApiCall("$BASE_URL/browse/new-releases?country=US&limit=8", callback) { response ->
            parseNewReleasesResponse(response)
        }
    }

    fun getFeaturedPlaylists(callback: SpotifyCallback<List<Music>>) {
        makeApiCall("$BASE_URL/browse/featured-playlists?country=US&limit=8", callback) { response ->
            parseFeaturedPlaylistsResponse(response)
        }
    }

    fun getTopTracks(callback: SpotifyCallback<List<Music>>) {
        val playlists = listOf("37i9dQZEVXbMDoHDwVN2tF")
        makeApiCall("$BASE_URL/playlists/${playlists[0]}/tracks?limit=8", callback) { response ->
            parseTopTracksResponse(response)
        }
    }

    private fun makeApiCall(
        url: String,
        callback: SpotifyCallback<List<Music>>,
        parseFunction: (JSONObject) -> List<Music>
    ) {
        getValidToken(object : SpotifyCallback<String> {
            override fun onSuccess(token: String) {
                makeApiCallWithToken(url, token, callback, parseFunction)
            }
            override fun onError(error: String) {
                callback.onError(error)
            }
        })
    }

    private fun makeApiCallWithToken(
        url: String,
        token: String,
        callback: SpotifyCallback<List<Music>>,
        parseFunction: (JSONObject) -> List<Music>
    ) {
        Log.d("SpotifyAPI", "🔍 Making API call to: $url")

        val request = object : JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                Log.d("SpotifyAPI", "✅ API call successful for: ${url.substringAfterLast("/")}")
                try {
                    val musicList = parseFunction(response)
                    Log.d("SpotifyAPI", "📊 Parsed ${musicList.size} items")
                    callback.onSuccess(musicList)
                } catch (e: Exception) {
                    Log.e("SpotifyAPI", "❌ Parse error: ${e.message}", e)
                    callback.onError("Failed to parse response: ${e.message}")
                }
            },
            { error ->
                val code = error.networkResponse?.statusCode
                if (code == 401) {
                    Log.w("SpotifyAPI", "🔄 Token expired during API call, regenerating...")
                    clearSpotifyCache()
                    makeApiCall(url, callback, parseFunction) // Retry with new token
                } else {
                    val errorMessage = error.message ?: "Unknown error (status: $code)"
                    callback.onError("Failed to load data: $errorMessage")
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
        request.setShouldCache(false)
        queue.add(request)
    }

    // Keep existing parsing methods for new releases, featured playlists, etc.
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
                val images = playlist.getJSONArray("images")
                val imageUrl = if (images.length() > 0) images.getJSONObject(0).optString("url", "") else ""
                musicList.add(Music(
                    id = playlist.optString("id", ""),
                    title = playlist.optString("name", "Unknown Playlist"),
                    artist = "Spotify Curated",
                    album = playlist.optString("name", "Unknown Playlist"),
                    releaseYear = Calendar.getInstance().get(Calendar.YEAR),
                    genre = "Various",
                    coverImage = imageUrl,
                    averageRating = 4.0 + (i % 3) * 0.3,
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
                val artistName = if (artists.length() > 0) artists.getJSONObject(0).optString("name", "Unknown Artist") else "Unknown Artist"
                val images = album.getJSONArray("images")
                val imageUrl = if (images.length() > 0) images.getJSONObject(0).optString("url", "") else ""
                val releaseDate = album.optString("release_date", "")
                val releaseYear = try { releaseDate.split("-")[0].toInt() } catch (e: Exception) { Calendar.getInstance().get(Calendar.YEAR) }
                musicList.add(Music(
                    id = track.optString("id", ""),
                    title = track.optString("name", "Unknown Track"),
                    artist = artistName,
                    album = album.optString("name", "Unknown Album"),
                    releaseYear = releaseYear,
                    genre = "Popular",
                    coverImage = imageUrl,
                    averageRating = 4.2 + (i % 4) * 0.2,
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
        val artistName = if (artists.length() > 0) artists.getJSONObject(0).optString("name", "Unknown Artist") else "Unknown Artist"
        val images = album.getJSONArray("images")
        val imageUrl = if (images.length() > 0) images.getJSONObject(0).optString("url", "") else ""
        val releaseDate = album.optString("release_date", "")
        val releaseYear = try { releaseDate.split("-")[0].toInt() } catch (e: Exception) { Calendar.getInstance().get(Calendar.YEAR) }
        return Music(
            id = album.optString("id", ""),
            title = album.optString("name", "Unknown Album"),
            artist = artistName,
            album = album.optString("name", "Unknown Album"),
            releaseYear = releaseYear,
            genre = "New Release",
            coverImage = imageUrl,
            averageRating = 4.0 + (Math.random() * 1.0),
            reviewCount = (Math.random() * 100 + 50).toInt()
        )
    }

    // Updated method names to reflect what they actually fetch
    fun getRecommendedAlbums(callback: SpotifyCallback<List<Music>>) {
        getNewReleases(callback)
    }

    fun getPopularAlbums(callback: SpotifyCallback<List<Music>>) {
        getTopTracks(callback)
    }

    fun getTrendingAlbums(callback: SpotifyCallback<List<Music>>) {
        getFeaturedPlaylists(callback)
    }
}