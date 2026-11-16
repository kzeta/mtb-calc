import Foundation

enum PressureCalculator {
    static let atmPsi = 14.696
    static let psiPerBar = 14.503773773
    static let refRiderKg = 75.0
    static let refBikeKg = 12.0
    static let frontShare = 0.42
    static let rearShare = 0.58
    static let refFrontPsi = 21.5
    static let refRearPsi = 24.0
    static let refWidthIn = 2.4

    static let defaultWheel = WheelSetup(
        widthIn: refWidthIn,
        rimInternalMm: 30,
        system: .tubeless,
        casing: .trail,
        maxPsi: nil
    )

    static func recommend(_ input: PressureInput) -> WheelAdvice {
        let share = input.position == .front ? frontShare : rearShare
        let refPsi = input.position == .front ? refFrontPsi : refRearPsi
        let total = input.riderWeightKg + input.bikeWeightKg
        let refTotal = refRiderKg + refBikeKg
        let base = refPsi * ((total * share) / (refTotal * share))
        let widthFactor = pow(refWidthIn / input.wheel.widthIn, 1.4)
        var psi = base * widthFactor
            + rimAdjust(input.wheel.rimInternalMm)
            + setupAdjust(input.wheel.system)
            + disciplineAdjust(input.discipline)
            + casingAdjust(input.wheel.casing)
            + terrainAdjust(input.terrain)

        let bounds = limits(input.wheel.system)
        var raised = false
        var limited = false
        if psi < bounds.0 {
            psi = bounds.0
            raised = true
        }
        if psi > bounds.1 {
            psi = bounds.1
            limited = true
        }
        if let maxPsi = input.wheel.maxPsi, psi > maxPsi {
            psi = maxPsi
            limited = true
            raised = false
        }
        return WheelAdvice(psi: psi, raisedToMinimum: raised, limitedByMax: limited)
    }

    static func garagePsi(trailPsi: Double, trailTempC: Double, garageTempC: Double) -> Double {
        let trailK = trailTempC + 273.15
        let garageK = garageTempC + 273.15
        return (trailPsi + atmPsi) * (garageK / trailK) - atmPsi
    }

    static func roundPsi(_ psi: Double) -> Double {
        floor(psi * 2.0 + 0.5) / 2.0
    }

    static func roundBar(_ psi: Double) -> Double {
        floor((psi / psiPerBar) * 20.0 + 0.5) / 20.0
    }

    static func displayRangePsi(_ advice: WheelAdvice, wheel: WheelSetup) -> (Double, Double) {
        let center = roundPsi(advice.psi)
        let bounds = limits(wheel.system)
        var low = max(center - 1.0, bounds.0)
        var high = min(center + 1.0, bounds.1)
        if let maxPsi = wheel.maxPsi {
            low = min(low, maxPsi)
            high = min(high, maxPsi)
        }
        if high < low { high = low }
        return (low, high)
    }

    static func formatPsi(_ psi: Double) -> String {
        let rounded = roundPsi(psi)
        if rounded.truncatingRemainder(dividingBy: 1) == 0 {
            return String(Int(rounded))
        }
        return String(format: "%.1f", locale: Locale(identifier: "es"), rounded)
    }

    static func formatBar(_ psi: Double) -> String {
        String(format: "%.2f", locale: Locale(identifier: "es"), roundBar(psi))
    }

    static func format(_ psi: Double, unit: PressureUnit) -> String {
        unit == .psi ? formatPsi(psi) : formatBar(psi)
    }

    static func formatKg(_ value: Double) -> String {
        let rounded = floor(value * 10.0 + 0.5) / 10.0
        if rounded.truncatingRemainder(dividingBy: 1) == 0 {
            return String(Int(rounded))
        }
        return String(format: "%.1f", locale: Locale(identifier: "es"), rounded)
    }

    static func toPsi(_ value: Double, unit: PressureUnit) -> Double {
        unit == .psi ? value : value * psiPerBar
    }

    private static func rimAdjust(_ mm: Double) -> Double {
        let delta = 30.0 - mm
        return delta > 0 ? delta * 0.15 : delta * 0.08
    }

    private static func setupAdjust(_ system: TireSystem) -> Double {
        switch system {
        case .tube: 4
        case .tubeless: 0
        case .insert: -2.5
        }
    }

    private static func disciplineAdjust(_ discipline: Discipline) -> Double {
        switch discipline {
        case .xc: 2
        case .marathon: 1.5
        case .trail: 0
        case .enduro: -1.5
        case .dh: -1
        }
    }

    private static func casingAdjust(_ casing: Casing) -> Double {
        switch casing {
        case .xc: 1.5
        case .trail: 0
        case .enduro: -1
        case .dh: -1.5
        }
    }

    private static func terrainAdjust(_ terrain: Terrain) -> Double {
        switch terrain {
        case .soft: -1.5
        case .mixed: 0
        case .dry: 0.5
        case .rock: 1.5
        }
    }

    private static func limits(_ system: TireSystem) -> (Double, Double) {
        switch system {
        case .tube: (18, 45)
        case .tubeless, .insert: (14, 40)
        }
    }
}
