package com.slior.data.remote

import com.slior.data.remote.dto.AuthResponse
import com.slior.data.remote.dto.LoginRequest
import com.slior.data.remote.dto.RegisterRequest
import com.slior.data.remote.dto.RouteResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Interfaz Retrofit que define los endpoints de la API REST de SLIOR.
 * Retrofit genera automáticamente la implementación en tiempo de ejecución.
 * Se irán añadiendo más endpoints en fases posteriores (rutas, paquetes...).
 */
interface ApiService {

    /** POST /auth/login → retorna JWT y datos del usuario */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    /** POST /auth/register → crea usuario y retorna JWT */
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @GET("api/routes/repartidor/{repartidorId}")
    suspend fun getRoutesByRepartidor(
        @Path("repartidorId") repartidorId: String
    ): List<RouteResponseDto>

    @GET("api/routes/{id}")
    suspend fun getRouteById(@Path("id") id: String): RouteResponseDto
}