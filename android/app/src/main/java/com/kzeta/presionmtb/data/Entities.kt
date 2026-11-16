package com.kzeta.presionmtb.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kzeta.presionmtb.domain.Bike
import com.kzeta.presionmtb.domain.Casing
import com.kzeta.presionmtb.domain.Discipline
import com.kzeta.presionmtb.domain.PressureLog
import com.kzeta.presionmtb.domain.PressureUnit
import com.kzeta.presionmtb.domain.RiderProfile
import com.kzeta.presionmtb.domain.Terrain
import com.kzeta.presionmtb.domain.TireSystem
import com.kzeta.presionmtb.domain.WheelSetup
import com.kzeta.presionmtb.domain.WheelSize

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val weightKg: Double,
    val heightCm: Double,
    val age: Int,
    val garageTempC: Double,
    val unit: String,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String?,
)

@Entity(tableName = "bikes")
data class BikeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val wheelSize: String,
    val weightKg: Double,
    val frontWidthIn: Double,
    val frontRimInternalMm: Double,
    val frontSystem: String,
    val frontCasing: String,
    val frontMaxPsi: Double?,
    val rearWidthIn: Double,
    val rearRimInternalMm: Double,
    val rearSystem: String,
    val rearCasing: String,
    val rearMaxPsi: Double?,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String?,
)

@Entity(tableName = "logs")
data class LogEntity(
    @PrimaryKey val id: String,
    val bikeId: String?,
    val bikeName: String,
    val placeName: String,
    val latitude: Double?,
    val longitude: Double?,
    val temperatureC: Double,
    val garageTempC: Double,
    val riderWeightKg: Double,
    val bikeWeightKg: Double,
    val discipline: String,
    val terrain: String,
    val frontRecommendedPsi: Double,
    val rearRecommendedPsi: Double,
    val frontUsedPsi: Double,
    val rearUsedPsi: Double,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String?,
)

fun RiderProfile.toEntity() = ProfileEntity(
    id, name, weightKg, heightCm, age, garageTempC, unit.wire, createdAt, updatedAt, deletedAt,
)

fun ProfileEntity.toDomain() = RiderProfile(
    id, name, weightKg, heightCm, age, garageTempC, PressureUnit.fromWire(unit), createdAt, updatedAt, deletedAt,
)

private fun wheel(width: Double, rim: Double, system: String, casing: String, maxPsi: Double?) =
    WheelSetup(width, rim, TireSystem.fromWire(system), Casing.fromWire(casing), maxPsi)

fun Bike.toEntity() = BikeEntity(
    id = id,
    name = name,
    wheelSize = wheelSize.wire,
    weightKg = weightKg,
    frontWidthIn = front.widthIn,
    frontRimInternalMm = front.rimInternalMm,
    frontSystem = front.system.wire,
    frontCasing = front.casing.wire,
    frontMaxPsi = front.maxPsi,
    rearWidthIn = rear.widthIn,
    rearRimInternalMm = rear.rimInternalMm,
    rearSystem = rear.system.wire,
    rearCasing = rear.casing.wire,
    rearMaxPsi = rear.maxPsi,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
)

fun BikeEntity.toDomain() = Bike(
    id = id,
    name = name,
    wheelSize = WheelSize.fromWire(wheelSize),
    weightKg = weightKg,
    front = wheel(frontWidthIn, frontRimInternalMm, frontSystem, frontCasing, frontMaxPsi),
    rear = wheel(rearWidthIn, rearRimInternalMm, rearSystem, rearCasing, rearMaxPsi),
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
)

fun PressureLog.toEntity() = LogEntity(
    id, bikeId, bikeName, placeName, latitude, longitude, temperatureC, garageTempC,
    riderWeightKg, bikeWeightKg, discipline.wire, terrain.wire, frontRecommendedPsi,
    rearRecommendedPsi, frontUsedPsi, rearUsedPsi, createdAt, updatedAt, deletedAt,
)

fun LogEntity.toDomain() = PressureLog(
    id, bikeId, bikeName, placeName, latitude, longitude, temperatureC, garageTempC,
    riderWeightKg, bikeWeightKg, Discipline.fromWire(discipline), Terrain.fromWire(terrain),
    frontRecommendedPsi, rearRecommendedPsi, frontUsedPsi, rearUsedPsi, createdAt, updatedAt, deletedAt,
)
