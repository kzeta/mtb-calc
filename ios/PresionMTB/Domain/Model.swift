import Foundation

enum WheelPosition: String, Codable {
    case front, rear
}

enum TireSystem: String, Codable, CaseIterable, Identifiable {
    case tube, tubeless, insert
    var id: String { rawValue }
    var label: String {
        switch self {
        case .tube: "Cámara"
        case .tubeless: "Tubeless"
        case .insert: "Tubeless con inserto"
        }
    }
}

enum Casing: String, Codable, CaseIterable, Identifiable {
    case xc, trail, enduro, dh
    var id: String { rawValue }
    var label: String {
        switch self {
        case .xc: "XC ligera"
        case .trail: "Trail"
        case .enduro: "Enduro reforzada"
        case .dh: "Doble capa"
        }
    }
}

enum Discipline: String, Codable, CaseIterable, Identifiable {
    case xc, marathon, trail, enduro, dh
    var id: String { rawValue }
    var label: String {
        switch self {
        case .xc: "XC"
        case .marathon: "Maratón"
        case .trail: "Trail"
        case .enduro: "Enduro"
        case .dh: "Descenso"
        }
    }
}

enum Terrain: String, Codable, CaseIterable, Identifiable {
    case soft, mixed, dry, rock
    var id: String { rawValue }
    var label: String {
        switch self {
        case .soft: "Blando"
        case .mixed: "Mixto"
        case .dry: "Seco"
        case .rock: "Roca y raíces"
        }
    }
}

enum WheelSize: String, Codable, CaseIterable, Identifiable {
    case in26 = "26"
    case in275 = "27.5"
    case in29 = "29"
    case mullet
    var id: String { rawValue }
    var label: String {
        switch self {
        case .in26: "26\""
        case .in275: "27.5\""
        case .in29: "29\""
        case .mullet: "Mullet"
        }
    }
}

enum PressureUnit: String, Codable, CaseIterable, Identifiable {
    case psi, bar
    var id: String { rawValue }
    var label: String { rawValue }
}

struct WheelSetup: Codable, Equatable {
    var widthIn: Double
    var rimInternalMm: Double
    var system: TireSystem
    var casing: Casing
    var maxPsi: Double?
}

struct RiderProfile: Codable, Equatable {
    var id: String
    var name: String
    var weightKg: Double
    var heightCm: Double
    var age: Int
    var garageTempC: Double
    var unit: PressureUnit
    var createdAt: String
    var updatedAt: String
    var deletedAt: String?
}

struct Bike: Codable, Equatable, Identifiable {
    var id: String
    var name: String
    var wheelSize: WheelSize
    var weightKg: Double
    var front: WheelSetup
    var rear: WheelSetup
    var createdAt: String
    var updatedAt: String
    var deletedAt: String?
}

struct PressureLog: Codable, Equatable, Identifiable {
    var id: String
    var bikeId: String?
    var bikeName: String
    var placeName: String
    var latitude: Double?
    var longitude: Double?
    var temperatureC: Double
    var garageTempC: Double
    var riderWeightKg: Double
    var bikeWeightKg: Double
    var discipline: Discipline
    var terrain: Terrain
    var frontRecommendedPsi: Double
    var rearRecommendedPsi: Double
    var frontUsedPsi: Double
    var rearUsedPsi: Double
    var createdAt: String
    var updatedAt: String
    var deletedAt: String?
}

struct SyncPayload: Codable, Equatable {
    var schemaVersion: Int
    var exportedAt: String
    var profile: RiderProfile?
    var bikes: [Bike]
    var logs: [PressureLog]
}

struct PressureInput {
    var riderWeightKg: Double
    var bikeWeightKg: Double
    var wheel: WheelSetup
    var position: WheelPosition
    var discipline: Discipline
    var terrain: Terrain
}

struct WheelAdvice: Equatable {
    var psi: Double
    var raisedToMinimum: Bool
    var limitedByMax: Bool
}

struct RideAdvice {
    var front: WheelAdvice
    var rear: WheelAdvice
    var frontGaragePsi: Double
    var rearGaragePsi: Double
}

func parseDecimal(_ text: String) -> Double? {
    Double(text.trimmingCharacters(in: .whitespacesAndNewlines).replacingOccurrences(of: ",", with: "."))
}
