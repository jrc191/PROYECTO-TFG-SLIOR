package com.slior.ui.routes

import com.slior.data.local.entity.RouteEntity

sealed class RouteListState {
    object Loading : RouteListState()
    data class Success(val routes: List<RouteEntity>) : RouteListState()
    data class Error(val message: String) : RouteListState()
}