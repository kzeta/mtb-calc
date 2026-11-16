import Foundation
import SwiftData

@Model
final class RiderProfileRecord {
    var recordId: String
    var name: String
    var weightKg: Double
    var heightCm: Double
    var age: Int
    var garageTempC: Double
    var unitRaw: String
    var createdAt: String
    var updatedAt: String
    var deletedAt: String?

    init(_ profile: RiderProfile) {
        recordId = profile.id
        name = profile.name
        weightKg = profile.weightKg
        heightCm = profile.heightCm
        age = profile.age
        garageTempC = profile.garageTempC
        unitRaw = profile.unit.rawValue
        createdAt = profile.createdAt
        updatedAt = profile.updatedAt
        deletedAt = profile.deletedAt
    }

    func apply(_ profile: RiderProfile) {
        recordId = profile.id
        name = profile.name
        weightKg = profile.weightKg
        heightCm = profile.heightCm
        age = profile.age
        garageTempC = profile.garageTempC
        unitRaw = profile.unit.rawValue
        createdAt = profile.createdAt
        updatedAt = profile.updatedAt
        deletedAt = profile.deletedAt
    }

    func toDomain() -> RiderProfile {
        RiderProfile(
            id: recordId,
            name: name,
            weightKg: weightKg,
            heightCm: heightCm,
            age: age,
            garageTempC: garageTempC,
            unit: PressureUnit(rawValue: unitRaw) ?? .psi,
            createdAt: createdAt,
            updatedAt: updatedAt,
            deletedAt: deletedAt
        )
    }
}

@Model
final class BikeRecord {
    var recordId: String
    var name: String
    var wheelSizeRaw: String
    var weightKg: Double
    var frontWidthIn: Double
    var frontRimInternalMm: Double
    var frontSystemRaw: String
    var frontCasingRaw: String
    var frontMaxPsi: Double?
    var rearWidthIn: Double
    var rearRimInternalMm: Double
    var rearSystemRaw: String
    var rearCasingRaw: String
    var rearMaxPsi: Double?
    var createdAt: String
    var updatedAt: String
    var deletedAt: String?

    init(_ bike: Bike) {
        recordId = bike.id
        name = bike.name
        wheelSizeRaw = bike.wheelSize.rawValue
        weightKg = bike.weightKg
        frontWidthIn = bike.front.widthIn
        frontRimInternalMm = bike.front.rimInternalMm
        frontSystemRaw = bike.front.system.rawValue
        frontCasingRaw = bike.front.casing.rawValue
        frontMaxPsi = bike.front.maxPsi
        rearWidthIn = bike.rear.widthIn
        rearRimInternalMm = bike.rear.rimInternalMm
        rearSystemRaw = bike.rear.system.rawValue
        rearCasingRaw = bike.rear.casing.rawValue
        rearMaxPsi = bike.rear.maxPsi
        createdAt = bike.createdAt
        updatedAt = bike.updatedAt
        deletedAt = bike.deletedAt
    }

    func apply(_ bike: Bike) {
        let fresh = BikeRecord(bike)
        recordId = fresh.recordId
        name = fresh.name
        wheelSizeRaw = fresh.wheelSizeRaw
        weightKg = fresh.weightKg
        frontWidthIn = fresh.frontWidthIn
        frontRimInternalMm = fresh.frontRimInternalMm
        frontSystemRaw = fresh.frontSystemRaw
        frontCasingRaw = fresh.frontCasingRaw
        frontMaxPsi = fresh.frontMaxPsi
        rearWidthIn = fresh.rearWidthIn
        rearRimInternalMm = fresh.rearRimInternalMm
        rearSystemRaw = fresh.rearSystemRaw
        rearCasingRaw = fresh.rearCasingRaw
        rearMaxPsi = fresh.rearMaxPsi
        createdAt = fresh.createdAt
        updatedAt = fresh.updatedAt
        deletedAt = fresh.deletedAt
    }

    func toDomain() -> Bike {
        Bike(
            id: recordId,
            name: name,
            wheelSize: WheelSize(rawValue: wheelSizeRaw) ?? .in29,
            weightKg: weightKg,
            front: WheelSetup(
                widthIn: frontWidthIn,
                rimInternalMm: frontRimInternalMm,
                system: TireSystem(rawValue: frontSystemRaw) ?? .tubeless,
                casing: Casing(rawValue: frontCasingRaw) ?? .trail,
                maxPsi: frontMaxPsi
            ),
            rear: WheelSetup(
                widthIn: rearWidthIn,
                rimInternalMm: rearRimInternalMm,
                system: TireSystem(rawValue: rearSystemRaw) ?? .tubeless,
                casing: Casing(rawValue: rearCasingRaw) ?? .trail,
                maxPsi: rearMaxPsi
            ),
            createdAt: createdAt,
            updatedAt: updatedAt,
            deletedAt: deletedAt
        )
    }
}

@Model
final class PressureLogRecord {
    var recordId: String
    var bikeId: String?
    var bikeName: String
    var placeName: String
    var latitude: Double?
    var longitude: Double?
    var temperatureC: Double
    var garageTempC: Double
    var riderWeightKg: Double
    var bikeWeightKg: Double
    var disciplineRaw: String
    var terrainRaw: String
    var frontRecommendedPsi: Double
    var rearRecommendedPsi: Double
    var frontUsedPsi: Double
    var rearUsedPsi: Double
    var createdAt: String
    var updatedAt: String
    var deletedAt: String?

    init(_ log: PressureLog) {
        recordId = log.id
        bikeId = log.bikeId
        bikeName = log.bikeName
        placeName = log.placeName
        latitude = log.latitude
        longitude = log.longitude
        temperatureC = log.temperatureC
        garageTempC = log.garageTempC
        riderWeightKg = log.riderWeightKg
        bikeWeightKg = log.bikeWeightKg
        disciplineRaw = log.discipline.rawValue
        terrainRaw = log.terrain.rawValue
        frontRecommendedPsi = log.frontRecommendedPsi
        rearRecommendedPsi = log.rearRecommendedPsi
        frontUsedPsi = log.frontUsedPsi
        rearUsedPsi = log.rearUsedPsi
        createdAt = log.createdAt
        updatedAt = log.updatedAt
        deletedAt = log.deletedAt
    }

    func apply(_ log: PressureLog) {
        let fresh = PressureLogRecord(log)
        recordId = fresh.recordId
        bikeId = fresh.bikeId
        bikeName = fresh.bikeName
        placeName = fresh.placeName
        latitude = fresh.latitude
        longitude = fresh.longitude
        temperatureC = fresh.temperatureC
        garageTempC = fresh.garageTempC
        riderWeightKg = fresh.riderWeightKg
        bikeWeightKg = fresh.bikeWeightKg
        disciplineRaw = fresh.disciplineRaw
        terrainRaw = fresh.terrainRaw
        frontRecommendedPsi = fresh.frontRecommendedPsi
        rearRecommendedPsi = fresh.rearRecommendedPsi
        frontUsedPsi = fresh.frontUsedPsi
        rearUsedPsi = fresh.rearUsedPsi
        createdAt = fresh.createdAt
        updatedAt = fresh.updatedAt
        deletedAt = fresh.deletedAt
    }

    func toDomain() -> PressureLog {
        PressureLog(
            id: recordId,
            bikeId: bikeId,
            bikeName: bikeName,
            placeName: placeName,
            latitude: latitude,
            longitude: longitude,
            temperatureC: temperatureC,
            garageTempC: garageTempC,
            riderWeightKg: riderWeightKg,
            bikeWeightKg: bikeWeightKg,
            discipline: Discipline(rawValue: disciplineRaw) ?? .trail,
            terrain: Terrain(rawValue: terrainRaw) ?? .mixed,
            frontRecommendedPsi: frontRecommendedPsi,
            rearRecommendedPsi: rearRecommendedPsi,
            frontUsedPsi: frontUsedPsi,
            rearUsedPsi: rearUsedPsi,
            createdAt: createdAt,
            updatedAt: updatedAt,
            deletedAt: deletedAt
        )
    }
}
