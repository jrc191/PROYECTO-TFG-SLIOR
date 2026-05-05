package com.slior.util

object Validators {

    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return email.matches(emailPattern.toRegex())
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

    fun isValidDate(date: String): Boolean {
        if (date.isBlank()) return false
        val datePattern = "^\\d{4}-\\d{2}-\\d{2}$"
        if (!date.matches(datePattern.toRegex())) return false
        
        return try {
            val parts = date.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].toInt()
            
            month in 1..12 && day in 1..31 && year >= 2000
        } catch (e: Exception) {
            false
        }
    }

    fun isValidSpanishPhone(phone: String): Boolean {
        if (phone.isBlank()) return false
        val spanishPhonePattern = "^(\\+34|0034|\\+34\\s|0034\\s)?[6789]\\d{8}$".replace("\\s+", "")
        return phone.replace("\\s+".toRegex(), "").matches(spanishPhonePattern.toRegex())
    }

    fun isValidLatitude(lat: String): Boolean {
        return try {
            val value = lat.toDouble()
            value >= -90 && value <= 90
        } catch (e: Exception) {
            false
        }
    }

    fun isValidLongitude(lon: String): Boolean {
        return try {
            val value = lon.toDouble()
            value >= -180 && value <= 180
        } catch (e: Exception) {
            false
        }
    }

    fun isValidCoordinates(lat: String, lon: String): Boolean {
        return isValidLatitude(lat) && isValidLongitude(lon)
    }

    fun isValidFieldLength(text: String, maxLength: Int): Boolean {
        return text.length <= maxLength
    }

    fun getValidationError(field: String, value: String): String? = when (field) {
        "email" -> when {
            value.isBlank() -> "El correo no puede estar vacío"
            !isValidEmail(value) -> "Formato de correo inválido"
            else -> null
        }
        "password" -> when {
            value.isBlank() -> "La contraseña no puede estar vacía"
            !isValidPassword(value) -> "La contraseña debe tener al menos 8 caracteres"
            else -> null
        }
        "fecha" -> when {
            value.isBlank() -> "La fecha no puede estar vacía"
            !isValidDate(value) -> "Formato de fecha inválido (usa YYYY-MM-DD)"
            else -> null
        }
        "telefono" -> when {
            value.isBlank() -> "El teléfono no puede estar vacío"
            !isValidSpanishPhone(value) -> "Teléfono inválido (ej: 600000000 o +34600000000)"
            else -> null
        }
        "latitud" -> when {
            value.isBlank() -> "La latitud no puede estar vacía"
            !isValidLatitude(value) -> "Latitud inválida (debe estar entre -90 y 90)"
            else -> null
        }
        "longitud" -> when {
            value.isBlank() -> "La longitud no puede estar vacía"
            !isValidLongitude(value) -> "Longitud inválida (debe estar entre -180 y 180)"
            else -> null
        }
        else -> null
    }
}
