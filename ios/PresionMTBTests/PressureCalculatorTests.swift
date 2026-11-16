import XCTest
@testable import PresionMTB

final class PressureCalculatorTests: XCTestCase {
    func testReferenceFrontAndRear() {
        let front = PressureCalculator.recommend(reference(.front))
        let rear = PressureCalculator.recommend(reference(.rear))
        XCTAssertEqual(front.psi, 21.5, accuracy: 0.0001)
        XCTAssertEqual(rear.psi, 24.0, accuracy: 0.0001)
        XCTAssertFalse(front.raisedToMinimum)
        XCTAssertFalse(front.limitedByMax)
    }

    func testTubeRimAndSidewall() {
        var tubeInput = reference(.rear)
        tubeInput.wheel.system = .tube
        XCTAssertEqual(PressureCalculator.recommend(tubeInput).psi, 28.0, accuracy: 0.0001)

        var rimInput = reference(.front)
        rimInput.wheel.rimInternalMm = 25
        XCTAssertEqual(PressureCalculator.recommend(rimInput).psi, 22.25, accuracy: 0.0001)

        var sidewall = reference(.rear)
        sidewall.wheel.maxPsi = 20
        let advice = PressureCalculator.recommend(sidewall)
        XCTAssertEqual(advice.psi, 20.0, accuracy: 0.0001)
        XCTAssertTrue(advice.limitedByMax)
    }

    func testGarageKeepsPressureWhenTemperaturesMatch() {
        XCTAssertEqual(PressureCalculator.garagePsi(trailPsi: 24, trailTempC: 20, garageTempC: 20), 24, accuracy: 0.0001)
    }

    func testFixturesMatchTheSharedOracle() throws {
        let url = try XCTUnwrap(Bundle(for: Self.self).url(forResource: "pressure-cases", withExtension: "json"))
        let file = try JSONDecoder().decode(FixtureFile.self, from: Data(contentsOf: url))
        for item in file.cases {
            let advice = PressureCalculator.recommend(item.input)
            XCTAssertEqual(advice.psi, item.expectedPsi, accuracy: 0.0001, item.name)
            XCTAssertEqual(advice.raisedToMinimum, item.raisedToMinimum, item.name)
            XCTAssertEqual(advice.limitedByMax, item.limitedByMax, item.name)
        }
        for item in file.garage {
            let psi = PressureCalculator.garagePsi(trailPsi: item.trailPsi, trailTempC: item.trailTempC, garageTempC: item.garageTempC)
            XCTAssertEqual(psi, item.expectedPsi, accuracy: 0.0001, item.name)
        }
        for item in file.rounding {
            XCTAssertEqual(PressureCalculator.roundPsi(item.psi), item.displayPsi, accuracy: 0.0001)
            XCTAssertEqual(PressureCalculator.roundBar(item.psi), item.displayBar, accuracy: 0.0001)
        }
    }

    func testMergeKeepsTheNewerWrite() {
        let local = bike(updated: "2026-11-16T18:00:00Z", name: "Trail")
        let older = bike(updated: "2026-11-16T12:00:00Z", name: "Vieja")
        let kept = SyncCodec.merge(
            local: SyncPayload(schemaVersion: 1, exportedAt: "2026-11-16T18:00:00Z", profile: nil, bikes: [local], logs: []),
            incoming: SyncPayload(schemaVersion: 1, exportedAt: "2026-11-16T12:00:00Z", profile: nil, bikes: [older], logs: [])
        )
        XCTAssertEqual(kept.bikes.first?.name, "Trail")
        var deleted = local
        deleted.deletedAt = "2026-11-16T19:00:00Z"
        deleted.updatedAt = "2026-11-16T19:00:00Z"
        let gone = SyncCodec.merge(
            local: SyncPayload(schemaVersion: 1, exportedAt: "2026-11-16T18:00:00Z", profile: nil, bikes: [local], logs: []),
            incoming: SyncPayload(schemaVersion: 1, exportedAt: "2026-11-16T19:00:00Z", profile: nil, bikes: [deleted], logs: [])
        )
        XCTAssertEqual(gone.bikes.first?.deletedAt, "2026-11-16T19:00:00Z")
    }

    private func reference(_ position: WheelPosition) -> PressureInput {
        PressureInput(
            riderWeightKg: 75,
            bikeWeightKg: 12,
            wheel: WheelSetup(widthIn: 2.4, rimInternalMm: 30, system: .tubeless, casing: .trail, maxPsi: nil),
            position: position,
            discipline: .trail,
            terrain: .mixed
        )
    }

    private func bike(updated: String, name: String) -> Bike {
        Bike(
            id: "22222222-2222-2222-2222-222222222222",
            name: name,
            wheelSize: .in29,
            weightKg: 12.5,
            front: WheelSetup(widthIn: 2.4, rimInternalMm: 30, system: .tubeless, casing: .trail, maxPsi: nil),
            rear: WheelSetup(widthIn: 2.4, rimInternalMm: 30, system: .tubeless, casing: .enduro, maxPsi: 35),
            createdAt: "2026-11-01T00:00:00Z",
            updatedAt: updated,
            deletedAt: nil
        )
    }
}

private struct FixtureFile: Decodable {
    var cases: [FixtureCase]
    var garage: [GarageFixture]
    var rounding: [RoundingFixture]
}

private struct FixtureCase: Decodable {
    var name: String
    var riderWeightKg: Double
    var bikeWeightKg: Double
    var widthIn: Double
    var rimInternalMm: Double
    var system: String
    var casing: String
    var discipline: String
    var terrain: String
    var maxPsi: Double?
    var position: String
    var expectedPsi: Double
    var raisedToMinimum: Bool
    var limitedByMax: Bool

    var input: PressureInput {
        PressureInput(
            riderWeightKg: riderWeightKg,
            bikeWeightKg: bikeWeightKg,
            wheel: WheelSetup(
                widthIn: widthIn,
                rimInternalMm: rimInternalMm,
                system: TireSystem(rawValue: system) ?? .tubeless,
                casing: Casing(rawValue: casing) ?? .trail,
                maxPsi: maxPsi
            ),
            position: position == "front" ? .front : .rear,
            discipline: Discipline(rawValue: discipline) ?? .trail,
            terrain: Terrain(rawValue: terrain) ?? .mixed
        )
    }
}

private struct GarageFixture: Decodable {
    var name: String
    var trailPsi: Double
    var trailTempC: Double
    var garageTempC: Double
    var expectedPsi: Double
}

private struct RoundingFixture: Decodable {
    var psi: Double
    var displayPsi: Double
    var displayBar: Double
}
