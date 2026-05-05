package com.slior.ui.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slior.data.local.dao.RouteDao
import com.slior.data.remote.dto.AddressSuggestion
import com.slior.data.remote.dto.CreateRouteRequest
import com.slior.data.repository.AuthRepository
import com.slior.data.repository.GeocodeService
import com.slior.data.repository.RouteRepository
import com.slior.util.LocationHelper
import com.slior.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val routeRepository: RouteRepository,
    private val authRepository: AuthRepository,
    private val routeDao: RouteDao,
    private val locationHelper: LocationHelper,
    private val geocodeService: GeocodeService
) : ViewModel() {

    private val _listState = MutableStateFlow<RouteListState>(RouteListState.Loading)
    val listState: StateFlow<RouteListState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow<RouteDetailState>(RouteDetailState.Loading)
    val detailState: StateFlow<RouteDetailState> = _detailState.asStateFlow()

    private val _createState = MutableStateFlow<CreateRouteState>(CreateRouteState.Idle)
    val createState: StateFlow<CreateRouteState> = _createState.asStateFlow()

    private val _addressSuggestions = MutableStateFlow<List<AddressSuggestion>>(emptyList())
    val addressSuggestions: StateFlow<List<AddressSuggestion>> = _addressSuggestions.asStateFlow()

    private val _isSearchingAddresses = MutableStateFlow(false)
    val isSearchingAddresses: StateFlow<Boolean> = _isSearchingAddresses.asStateFlow()

    private val _addressSearchError = MutableStateFlow<String?>(null)
    val addressSearchError: StateFlow<String?> = _addressSearchError.asStateFlow()

    private var addressSearchJob: Job? = null

    fun loadRoutes(repartidorId: String) {
        viewModelScope.launch {
            _listState.value = RouteListState.Loading

            // Intentar sync con el servidor
            val syncResult = routeRepository.syncRoutes(repartidorId)

            // Siempre leer de Room (offline-first)
            routeRepository.getRoutesByRepartidor(repartidorId)
                .collect { routes ->
                    _listState.value = when {
                        syncResult is Result.Success || routes.isNotEmpty() ->
                            RouteListState.Success(routes)
                        syncResult is Result.Error ->
                            RouteListState.Error(
                                message = syncResult.exception.message ?: "Error de conexión",
                                cachedRoutes = routes
                            )
                        else -> RouteListState.Success(routes)
                    }
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
                
                // Obtener userId y luego el usuario para extraer vehicleType
                val userId = authRepository.getSavedUserId()
                if (userId == null) {
                    _detailState.value = RouteDetailState.Error("Usuario no autenticado")
                    return@launch
                }
                
                // Obtener el usuario y su tipo de vehículo
                authRepository.getCurrentUser(userId).collect { user ->
                    if (user == null) {
                        _detailState.value = RouteDetailState.Error("Usuario no encontrado")
                        return@collect
                    }
                    
                    val vehicleType = user.vehicleType
                    routeRepository.optimizeRoute(routeId, lat, lon, vehicleType)
                    loadRouteDetail(routeId)
                }
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

    fun searchAddressSuggestions(query: String) {
        addressSearchJob?.cancel()

        val normalizedQuery = query.trim()
        if (normalizedQuery.length < 3) {
            _addressSuggestions.value = emptyList()
            _addressSearchError.value = null
            _isSearchingAddresses.value = false
            return
        }

        addressSearchJob = viewModelScope.launch {
            delay(350)
            _isSearchingAddresses.value = true
            _addressSearchError.value = null

            when (val result = geocodeService.searchAddresses(normalizedQuery)) {
                is Result.Success -> {
                    _addressSuggestions.value = result.data.take(8)
                }
                is Result.Error -> {
                    _addressSuggestions.value = emptyList()
                    _addressSearchError.value = result.exception.message ?: "No se pudo buscar direcciones"
                }
                else -> Unit
            }

            _isSearchingAddresses.value = false
        }
    }

    fun clearAddressSuggestions() {
        _addressSuggestions.value = emptyList()
        _addressSearchError.value = null
        _isSearchingAddresses.value = false
        addressSearchJob?.cancel()
    }

    // ── Drawer state ──────────────────────────────────────────────────────────
    private val _drawerOpen = MutableStateFlow<DrawerType?>(null)
    val drawerOpen: StateFlow<DrawerType?> = _drawerOpen.asStateFlow()

    fun openDrawer(type: DrawerType) { _drawerOpen.value = type }
    fun closeDrawer() { _drawerOpen.value = null }

    enum class DrawerType { MENU, PROFILE }
}
