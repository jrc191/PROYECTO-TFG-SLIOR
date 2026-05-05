package com.slior.util

object RealAddresses {
    val spanishAddresses = listOf(
        AddressData("Calle Falsa 123", "Madrid", 40.4168, -3.7038),
        AddressData("Av. Siempreviva 742", "Madrid", 40.4312, -3.6919),
        AddressData("Boulevard de los Sueños Rotos 404", "Valencia", 39.4699, -0.3763),
        AddressData("Plaza de la República 10", "Barcelona", 41.3874, 2.1686),
        AddressData("Callejón Diagon 99", "Bilbao", 43.2630, -2.9350),
        AddressData("Calle Gran Vía 560", "Madrid", 40.4175, -3.7137),
        AddressData("Paseo de la Castellana 200", "Madrid", 40.4538, -3.6883),
        AddressData("Ronda Sant Antoni 45", "Barcelona", 41.3824, 2.1660),
        AddressData("Calle Serrano 89", "Madrid", 40.4280, -3.6850),
        AddressData("Avenida Portugal 15", "Valencia", 39.4719, -0.3619),
        AddressData("Calle Xativa 22", "Valencia", 39.4720, -0.3745),
        AddressData("Paseo Marítimo 12", "Barcelona", 41.3851, 2.1734),
        AddressData("Calle Balmes 280", "Barcelona", 41.4048, 2.1564),
        AddressData("Ronda Universidad 3", "Barcelona", 41.3869, 2.1688),
        AddressData("Calle Aribau 156", "Barcelona", 41.3926, 2.1561),
    )

    data class AddressData(
        val direccion: String,
        val ciudad: String,
        val latitud: Double,
        val longitud: Double
    )

    fun getRandomAddresses(count: Int): List<AddressData> {
        return spanishAddresses.shuffled().take(count)
    }

    fun getAddressByIndex(index: Int): AddressData {
        return spanishAddresses[index % spanishAddresses.size]
    }
}
