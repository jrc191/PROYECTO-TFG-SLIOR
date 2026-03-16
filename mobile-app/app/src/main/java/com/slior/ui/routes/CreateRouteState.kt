package com.slior.ui.routes

import com.slior.data.remote.dto.AddressSuggestion

sealed class CreateRouteState {
    object Idle : CreateRouteState()
    object Loading : CreateRouteState()
    object Success : CreateRouteState()
    data class Error(val message: String) : CreateRouteState()
    data class AddressSearchResults(
        val suggestions: List<AddressSuggestion>,
        val isLoading: Boolean
    ) : CreateRouteState()
}