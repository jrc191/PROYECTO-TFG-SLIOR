package com.slior.ui.routes

sealed class CreateRouteState {
    object Idle : CreateRouteState()
    object Loading : CreateRouteState()
    object Success : CreateRouteState()
    data class Error(val message: String) : CreateRouteState()
}