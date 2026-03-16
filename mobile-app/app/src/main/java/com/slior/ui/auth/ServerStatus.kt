package com.slior.ui.auth

/** Estado de conectividad con el servidor SLIOR. */
sealed class ServerStatus {
    object Checking : ServerStatus()
    object Online   : ServerStatus()
    object Offline  : ServerStatus()
}
