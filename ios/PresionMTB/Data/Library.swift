import Foundation
import SwiftData

enum Library {
    static func profile(in context: ModelContext) -> RiderProfileRecord? {
        let rows = (try? context.fetch(FetchDescriptor<RiderProfileRecord>())) ?? []
        return rows.filter { $0.deletedAt == nil }.max { $0.updatedAt < $1.updatedAt }
    }

    static func bikes(in context: ModelContext) -> [BikeRecord] {
        let rows = (try? context.fetch(FetchDescriptor<BikeRecord>())) ?? []
        return rows.filter { $0.deletedAt == nil }.sorted { $0.name.localizedCaseInsensitiveCompare($1.name) == .orderedAscending }
    }

    static func logs(in context: ModelContext) -> [PressureLogRecord] {
        let rows = (try? context.fetch(FetchDescriptor<PressureLogRecord>())) ?? []
        return rows.filter { $0.deletedAt == nil }.sorted { $0.createdAt > $1.createdAt }
    }

    static func bike(id: String, in context: ModelContext) -> BikeRecord? {
        bikes(in: context).first { $0.recordId == id }
    }

    static func save(profile: RiderProfile, in context: ModelContext) throws {
        let rows = (try? context.fetch(FetchDescriptor<RiderProfileRecord>())) ?? []
        rows.forEach { context.delete($0) }
        context.insert(RiderProfileRecord(profile))
        try context.save()
    }

    static func save(bike: Bike, in context: ModelContext) throws {
        if let existing = ((try? context.fetch(FetchDescriptor<BikeRecord>())) ?? []).first(where: { $0.recordId == bike.id }) {
            existing.apply(bike)
        } else {
            context.insert(BikeRecord(bike))
        }
        try context.save()
    }

    static func save(log: PressureLog, in context: ModelContext) throws {
        if let existing = ((try? context.fetch(FetchDescriptor<PressureLogRecord>())) ?? []).first(where: { $0.recordId == log.id }) {
            existing.apply(log)
        } else {
            context.insert(PressureLogRecord(log))
        }
        try context.save()
    }

    static func softDelete(_ bike: BikeRecord, in context: ModelContext) throws {
        let now = AppTime.now()
        bike.deletedAt = now
        bike.updatedAt = now
        try context.save()
    }

    static func softDelete(_ log: PressureLogRecord, in context: ModelContext) throws {
        let now = AppTime.now()
        log.deletedAt = now
        log.updatedAt = now
        try context.save()
    }

    static func exportData(in context: ModelContext) throws -> Data {
        let payload = try snapshot(in: context)
        return Data(try SyncCodec.export(payload).utf8)
    }

    static func importData(_ data: Data, in context: ModelContext) throws {
        let raw = String(decoding: data, as: UTF8.self)
        let incoming = try SyncCodec.parse(raw)
        let merged = SyncCodec.merge(local: try snapshot(in: context), incoming: incoming)
        let profiles = (try? context.fetch(FetchDescriptor<RiderProfileRecord>())) ?? []
        profiles.forEach { context.delete($0) }
        if let profile = merged.profile {
            context.insert(RiderProfileRecord(profile))
        }
        let bikes = (try? context.fetch(FetchDescriptor<BikeRecord>())) ?? []
        var bikeById = Dictionary(uniqueKeysWithValues: bikes.map { ($0.recordId, $0) })
        for bike in merged.bikes {
            if let existing = bikeById.removeValue(forKey: bike.id) {
                existing.apply(bike)
            } else {
                context.insert(BikeRecord(bike))
            }
        }
        let logs = (try? context.fetch(FetchDescriptor<PressureLogRecord>())) ?? []
        var logById = Dictionary(uniqueKeysWithValues: logs.map { ($0.recordId, $0) })
        for log in merged.logs {
            if let existing = logById.removeValue(forKey: log.id) {
                existing.apply(log)
            } else {
                context.insert(PressureLogRecord(log))
            }
        }
        try context.save()
    }

    private static func snapshot(in context: ModelContext) throws -> SyncPayload {
        let profiles = (try? context.fetch(FetchDescriptor<RiderProfileRecord>())) ?? []
        let bikes = (try? context.fetch(FetchDescriptor<BikeRecord>())) ?? []
        let logs = (try? context.fetch(FetchDescriptor<PressureLogRecord>())) ?? []
        return SyncPayload(
            schemaVersion: 1,
            exportedAt: AppTime.now(),
            profile: profiles.max { $0.updatedAt < $1.updatedAt }?.toDomain(),
            bikes: bikes.map { $0.toDomain() },
            logs: logs.map { $0.toDomain() }
        )
    }
}
