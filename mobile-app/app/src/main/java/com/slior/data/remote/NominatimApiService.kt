package com.slior.data.remote

import com.slior.data.remote.dto.NominatimSearchResult
import retrofit2.http.GET
import retrofit2.http.Query

interface NominatimApiService {
    @GET("search")
    suspend fun searchAddresses(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 5
    ): List<NominatimSearchResult>

    companion object {
        const val BASE_URL = "https://nominatim.openstreetmap.org/"
    }
}
