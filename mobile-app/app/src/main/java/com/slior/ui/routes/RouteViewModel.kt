package com.slior.ui.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slior.data.repository.RouteRepository
import com.slior.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val routeRepository: RouteRepository
) : ViewModel() {

    private val _state = MutableStateFlow<RouteListState>(RouteListState.Loading)
    val state: StateFlow<RouteListState> = _state.asStateFlow()

    fun loadRoutes(repartidorId: String) {
        viewModelScope.launch {
            _state.value = RouteListState.Loading
            when (val result = routeRepository.syncRoutes(repartidorId)) {
                is Result.Success -> {
                    routeRepository.getRoutesByRepartidor(repartidorId)
                        .collect { routes ->
                            _state.value = RouteListState.Success(routes)
                        }
                }
                is Result.Error -> {
                    _state.value = RouteListState.Error(
                        result.exception.message ?: "Error desconocido"
                    )
                }
                else -> Unit
            }
        }
    }
}