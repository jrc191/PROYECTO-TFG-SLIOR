package com.slior.data.remote

import com.slior.data.remote.dto.PhotonResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PhotonApiService {
    @GET("api")
    suspend fun searchAddresses(
        @Query("q") query: String,
        @Query("limit") limit: Int = 5,
        @Query("lang") lang: String = "es"
    ): PhotonResponse
}

