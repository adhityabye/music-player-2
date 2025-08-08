package by.tutorials.musicapp.data.repository

import android.util.Log
import by.tutorials.musicapp.data.model.Song
import by.tutorials.musicapp.data.model.SongItem
import by.tutorials.musicapp.data.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SongRepository {

    private val itunesService = ApiClient.instance
//    private val TAG = "SongRepository"

    suspend fun searchSongs(searchTerm: String): Result<List<Song>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = itunesService.searchSongs(searchTerm = searchTerm)
                if (response.isSuccessful) {
                    val songItems = response.body()?.results ?: emptyList()
                    val songs = songItems.filter { it.kind == "song" && it.previewUrl != null }
                        .mapNotNull { mapSongItemToSong(it) }
                    Result.success(songs)
                } else {
//                    val errorBody = response.errorBody()?.string()
                    val attemptedUrl = response.raw().request.url.toString()
                    Result.failure(Exception("Error fetching songs: ${response.code()} ${response.message()} from $attemptedUrl"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Network error while searching for \"$searchTerm\": ${e.message}", e))
            }
        }
    }

    private fun mapSongItemToSong(item: SongItem): Song? {
        return if (item.trackId != null && item.trackName != null && item.artistName != null &&
            item.collectionName != null && item.previewUrl != null &&
            item.artworkUrl100 != null && item.trackTimeMillis != null && item.primaryGenreName != null) {
            Song(
                id = item.trackId,
                title = item.trackName,
                artist = item.artistName,
                album = item.collectionName,
                previewUrl = item.previewUrl,
                artworkUrl = item.artworkUrl100,
                duration = item.trackTimeMillis,
                genre = item.primaryGenreName
            )
        } else {
            null
        }
    }
}
