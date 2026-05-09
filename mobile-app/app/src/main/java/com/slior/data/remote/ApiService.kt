package com.slior.data.remote

import com.slior.data.remote.dto.AuthResponse
import com.slior.data.remote.dto.CreateRouteRequest
import com.slior.data.remote.dto.LoginRequest
import com.slior.data.remote.dto.RegisterRequest
import com.slior.data.remote.dto.RouteResponseDto
import com.slior.data.remote.dto.AddressSuggestion
import com.slior.data.remote.dto.OptimizeRouteRequest
import com.slior.data.remote.dto.UpdateRouteRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfaz Retrofit que define los endpoints de la API REST de SLIOR.
 * Retrofit genera automáticamente la implementación en tiempo de ejecución.
 * Se irán añadiendo más endpoints en fases posteriores (rutas, paquetes...).
 */
interface ApiService {

    @POST("auth/v1/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/v1/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    /** Comprueba si el servidor está activo. Cualquier respuesta HTTP (incluso 4xx)
     *  significa que el servidor está en línea; solo una IOException implica sin conexión. */
    @GET("health")
    suspend fun healthCheck(): retrofit2.Response<Unit>

    @GET("api/v1/routes/repartidor/{repartidorId}")
    suspend fun getRoutesByRepartidor(
        @Path("repartidorId") repartidorId: String
    ): List<RouteResponseDto>

    @GET("api/v1/routes/{id}")
    suspend fun getRouteById(@Path("id") id: String): RouteResponseDto

    @POST("api/v1/routes")
    suspend fun createRoute(@Body request: CreateRouteRequest): RouteResponseDto

    @PUT("api/v1/routes/{id}")
    suspend fun updateRoute(
        @Path("id") id: String,
        @Body request: UpdateRouteRequest
    ): RouteResponseDto

    @DELETE("api/v1/routes/{id}")
    suspend fun deleteRoute(@Path("id") id: String): retrofit2.Response<Unit>

    @POST("api/v1/routes/{id}/optimize")
    suspend fun optimizeRoute(
        @Path("id") routeId: String,
        @Body request: OptimizeRouteRequest
    ): RouteResponseDto

    @GET("api/v1/geocode/search")
    suspend fun searchAddresses(@Query("q") query: String): List<AddressSuggestion>

    @GET("api/v1/geocode/reverse")
    suspend fun reverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): AddressSuggestion
}
