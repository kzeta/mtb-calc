package com.kzeta.presionmtb.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class WheelPosition {
    @SerialName("front") FRONT,
    @SerialName("rear") REAR,
}

@Serializable
enum class TireSystem(val wire: String, val label: String) {
    @SerialName("tube") TUBE("tube", "Cámara"),
    @SerialName("tubeless") TUBELESS("tubeless", "Tubeless"),
    @SerialName("insert") INSERT("insert", "Tubeless con inserto"),
    ;

    companion object {
        fun fromWire(wire: String): TireSystem = entries.first { it.wire == wire }
    }
}

@Serializable
enum class Casing(val wire: String, val label: String) {
    @SerialName("xc") XC("xc", "XC ligera"),
    @SerialName("trail") TRAIL("trail", "Trail"),
    @SerialName("enduro") ENDURO("enduro", "Enduro reforzada"),
    @SerialName("dh") DH("dh", "Doble capa"),
    ;

    companion object {
        fun fromWire(wire: String): Casing = entries.first { it.wire == wire }
    }
}

@Serializable
enum class Discipline(val wire: String, val label: String) {
    @SerialName("xc") XC("xc", "XC"),
    @SerialName("marathon") MARATHON("marathon", "Maratón"),
    @SerialName("trail") TRAIL("trail", "Trail"),
    @SerialName("enduro") ENDURO("enduro", "Enduro"),
    @SerialName("dh") DH("dh", "Descenso"),
    ;

    companion object {
        fun fromWire(wire: String): Discipline = entries.first { it.wire == wire }
    }
}

@Serializable
enum class Terrain(val wire: String, val label: String) {
    @SerialName("soft") SOFT("soft", "Blando"),
    @SerialName("mixed") MIXED("mixed", "Mixto"),
    @SerialName("dry") DRY("dry", "Seco"),
    @SerialName("rock") ROCK("rock", "Roca y raíces"),
    ;

    companion object {
        fun fromWire(wire: String): Terrain = entries.first { it.wire == wire }
    }
}

@Serializable
enum class WheelSize(val wire: String, val label: String) {
    @SerialName("26") IN_26("26", "26\""),
    @SerialName("27.5") IN_275("27.5", "27.5\""),
    @SerialName("29") IN_29("29", "29\""),
    @SerialName("mullet") MULLET("mullet", "Mullet"),
    ;

    companion object {
        fun fromWire(wire: String): WheelSize = entries.first { it.wire == wire }
    }
}

@Serializable
enum class PressureUnit(val wire: String, val label: String) {
    @SerialName("psi") PSI("psi", "psi"),
    @SerialName("bar") BAR("bar", "bar"),
    ;

    companion object {
        fun fromWire(wire: String): PressureUnit = entries.first { it.wire == wire }
    }
}

@Serializable
data class WheelSetup(
    val widthIn: Double,
    val rimInternalMm: Double,
    val system: TireSystem,
    val casing: Casing,
    val maxPsi: Double? = null,
)

@Serializable
data class RiderProfile(
    val id: String,
    val name: String,
    val weightKg: Double,
    val heightCm: Double,
    val age: Int,
    val garageTempC: Double,
    val unit: PressureUnit,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String? = null,
)

@Serializable
data class Bike(
    val id: String,
    val name: String,
    val wheelSize: WheelSize,
    val weightKg: Double,
    val front: WheelSetup,
    val rear: WheelSetup,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String? = null,
)

@Serializable
data class PressureLog(
    val id: String,
    val bikeId: String?,
    val bikeName: String,
    val placeName: String,
    val latitude: Double?,
    val longitude: Double?,
    val temperatureC: Double,
    val garageTempC: Double,
    val riderWeightKg: Double,
    val bikeWeightKg: Double,
    val discipline: Discipline,
    val terrain: Terrain,
    val frontRecommendedPsi: Double,
    val rearRecommendedPsi: Double,
    val frontUsedPsi: Double,
    val rearUsedPsi: Double,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String? = null,
)

@Serializable
data class SyncPayload(
    val schemaVersion: Int = 1,
    val exportedAt: String,
    val profile: RiderProfile? = null,
    val bikes: List<Bike> = emptyList(),
    val logs: List<PressureLog> = emptyList(),
)

data class PressureInput(
    val riderWeightKg: Double,
    val bikeWeightKg: Double,
    val wheel: WheelSetup,
    val position: WheelPosition,
    val discipline: Discipline,
    val terrain: Terrain,
)

data class WheelAdvice(
    val psi: Double,
    val raisedToMinimum: Boolean,
    val limitedByMax: Boolean,
)

data class RideAdvice(
    val front: WheelAdvice,
    val rear: WheelAdvice,
    val frontGaragePsi: Double,
    val rearGaragePsi: Double,
)
