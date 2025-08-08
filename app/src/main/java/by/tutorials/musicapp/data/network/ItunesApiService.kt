package by.tutorials.musicapp.data.network

import by.tutorials.musicapp.data.model.SongApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApiService {
    @GET("search")
    suspend fun searchSongs(
        @Query("term") searchTerm: String
    ): Response<SongApiResponse>
}

