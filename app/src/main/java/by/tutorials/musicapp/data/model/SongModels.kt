package by.tutorials.musicapp.data.model

data class SongApiResponse(
    val resultCount: Int,
    val results: List<SongItem>
)

data class SongItem(
    val wrapperType: String?,
    val kind: String?,
    val trackId: Long?,
    val artistName: String?,
    val collectionName: String?,
    val trackName: String?,
    val previewUrl: String?,
    val artworkUrl30: String?,
    val artworkUrl60: String?,
    val artworkUrl100: String?,
    val trackTimeMillis: Long?,
    val primaryGenreName: String?
)

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val previewUrl: String,
    val artworkUrl: String,
    val duration: Long,
    val genre: String
)
