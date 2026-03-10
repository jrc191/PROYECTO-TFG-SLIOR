package com.slior.ui.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slior.data.local.dao.RouteDao
import com.slior.data.remote.dto.CreateRouteRequest
import com.slior.data.repository.RouteRepository
import com.slior.util.LocationHelper
import com.slior.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val routeRepository: RouteRepository,
    private val routeDao: RouteDao,
    private val locationHelper: LocationHelper
) : ViewModel() {

    private val _listState = MutableStateFlow<RouteListState>(RouteListState.Loading)
    val listState: StateFlow<RouteListState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow<RouteDetailState>(RouteDetailState.Loading)
    val detailState: StateFlow<RouteDetailState> = _detailState.asStateFlow()

    private val _createState = MutableStateFlow<CreateRouteState>(CreateRouteState.Idle)
    val createState: StateFlow<CreateRouteState> = _createState.asStateFlow()

    fun loadRoutes(repartidorId: String) {
        viewModelScope.launch {
            _listState.value = RouteListState.Loading
            when (val result = routeRepository.syncRoutes(repartidorId)) {
                is Result.Success -> {
                    routeRepository.getRoutesByRepartidor(repartidorId)
                        .collect { routes ->
                            _listState.value = RouteListState.Success(routes)
                        }
                }
                is Result.Error -> {
                    _listState.value = RouteListState.Error(
                        result.exception.message ?: "Error cargando rutas"
                    )
                }
                else -> Unit
            }
        }
    }

    fun loadRouteDetail(routeId: String) {
        viewModelScope.launch {
            _detailState.value = RouteDetailState.Loading
            val route = routeDao.getRouteById(routeId)
            if (route == null) {
                _detailState.value = RouteDetailState.Error("Ruta no encontrada")
                return@launch
            }
            routeRepository.getStopsByRoute(routeId).collect { stops ->
                _detailState.value = RouteDetailState.Success(route, stops)
            }
        }
    }

    fun optimizeRoute(routeId: String) {
        viewModelScope.launch {
            try {
                val (lat, lon) = locationHelper.getCurrentLocation()
                routeRepository.optimizeRoute(routeId, lat, lon)
                loadRouteDetail(routeId)
            } catch (e: Exception) {
                _detailState.value = RouteDetailState.Error(
                    e.message ?: "Error al optimizar"
                )
            }
        }
    }

    fun createRoute(request: CreateRouteRequest) {
        viewModelScope.launch {
            _createState.value = CreateRouteState.Loading
            _createState.value = when (val result = routeRepository.createRoute(request)) {
                is Result.Success -> CreateRouteState.Success
                is Result.Error -> CreateRouteState.Error(
                    result.exception.message ?: "Error al crear la ruta"
                )
                else -> CreateRouteState.Idle
            }
        }
    }

    fun resetCreateState() {
        _createState.value = CreateRouteState.Idle
    }
}