package com.slior.ui.routes

import com.slior.data.local.entity.RouteEntity
import com.slior.data.local.entity.StopEntity

sealed class RouteDetailState {
    object Loading : RouteDetailState()
    data class Success(
        val route: RouteEntity,
        val stops: List<StopEntity>
    ) : RouteDetailState()
    data class Error(val message: String) : RouteDetailState()
}