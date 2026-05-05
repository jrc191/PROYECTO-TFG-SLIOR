package com.slior.ui.routes

import com.slior.data.local.entity.RouteEntity
import com.slior.data.local.entity.StopEntity

sealed class NavigationState {
    object Loading : NavigationState()
    data class Success(val route: RouteEntity, val stops: List<StopEntity>) : NavigationState()
    data class Error(val message: String) : NavigationState()
}
