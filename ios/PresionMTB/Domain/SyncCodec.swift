import Foundation

enum AppTime {
    static func now() -> String {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        formatter.timeZone = TimeZone(secondsFromGMT: 0)
        return formatter.string(from: Date())
    }

    static func parse(_ value: String) -> Date {
        let fractional = ISO8601DateFormatter()
        fractional.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        if let date = fractional.date(from: value) { return date }
        let plain = ISO8601DateFormatter()
        plain.formatOptions = [.withInternetDateTime]
        return plain.date(from: value) ?? .distantPast
    }

    static func newId() -> String {
        UUID().uuidString.lowercased()
    }
}

enum SyncCodec {
    private static let encoder: JSONEncoder = {
        let encoder = JSONEncoder()
        encoder.outputFormatting = [.prettyPrinted, .sortedKeys]
        return encoder
    }()

    private static let decoder = JSONDecoder()

    static func export(_ payload: SyncPayload) throws -> String {
        let data = try encoder.encode(normalize(payload))
        return String(decoding: data, as: UTF8.self)
    }

    static func parse(_ raw: String) throws -> SyncPayload {
        guard let data = raw.data(using: .utf8) else {
            throw SyncError(message: "No se pudo leer el archivo.")
        }
        let payload = try decoder.decode(SyncPayload.self, from: data)
        if payload.schemaVersion != 1 {
            throw SyncError(message: "Este archivo es de una versión más nueva de la app.")
        }
        return normalize(payload)
    }

    static func merge(local: SyncPayload, incoming: SyncPayload) -> SyncPayload {
        SyncPayload(
            schemaVersion: 1,
            exportedAt: AppTime.now(),
            profile: newer(local.profile, incoming.profile, updatedAt: \.updatedAt),
            bikes: mergeLists(local.bikes, incoming.bikes, id: \.id, updatedAt: \.updatedAt),
            logs: mergeLists(local.logs, incoming.logs, id: \.id, updatedAt: \.updatedAt)
        )
    }

    private static func normalize(_ payload: SyncPayload) -> SyncPayload {
        var copy = payload
        if var profile = payload.profile {
            profile.id = profile.id.lowercased()
            copy.profile = profile
        } else {
            copy.profile = nil
        }
        copy.bikes = payload.bikes.map { bike in
            var item = bike
            item.id = bike.id.lowercased()
            return item
        }
        copy.logs = payload.logs.map { log in
            var item = log
            item.id = log.id.lowercased()
            item.bikeId = log.bikeId?.lowercased()
            return item
        }
        return copy
    }

    private static func newer<T>(_ local: T?, _ incoming: T?, updatedAt: (T) -> String) -> T? {
        guard let local else { return incoming }
        guard let incoming else { return local }
        return AppTime.parse(updatedAt(incoming)) >= AppTime.parse(updatedAt(local)) ? incoming : local
    }

    private static func mergeLists<T>(_ local: [T], _ incoming: [T], id: (T) -> String, updatedAt: (T) -> String) -> [T] {
        var merged: [String: T] = [:]
        var order: [String] = []
        for item in local {
            let key = id(item)
            if merged[key] == nil { order.append(key) }
            merged[key] = item
        }
        for item in incoming {
            let key = id(item)
            if merged[key] == nil { order.append(key) }
            if let current = merged[key], AppTime.parse(updatedAt(item)) < AppTime.parse(updatedAt(current)) {
                continue
            }
            merged[key] = item
        }
        return order.compactMap { merged[$0] }
    }
}

struct SyncError: LocalizedError {
    var message: String
    var errorDescription: String? { message }
}
