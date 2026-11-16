import SwiftData
import SwiftUI

struct CalculatorView: View {
    @Environment(\.modelContext) private var context
    @Environment(\.colorScheme) private var scheme
    @Query private var profiles: [RiderProfileRecord]
    @Query private var storedBikes: [BikeRecord]

    @State private var riderWeight = ""
    @State private var seeded = false
    @State private var selectedBikeId: String?
    @State private var discipline = Discipline.trail
    @State private var terrain = Terrain.mixed
    @State private var temperature = "20"
    @State private var placeName = ""
    @State private var latitude: Double?
    @State private var longitude: Double?
    @State private var loadingWeather = false
    @State private var message: String?
    @State private var saveOpen = false
    @State private var frontUsed = ""
    @State private var rearUsed = ""

    private var palette: Palette { Palette.current(scheme) }
    private var profile: RiderProfileRecord? {
        profiles.filter { $0.deletedAt == nil }.max { $0.updatedAt < $1.updatedAt }
    }
    private var bikes: [BikeRecord] {
        storedBikes.filter { $0.deletedAt == nil }.sorted { $0.name.localizedCaseInsensitiveCompare($1.name) == .orderedAscending }
    }
    private var selectedBike: BikeRecord? {
        bikes.first { $0.recordId == selectedBikeId } ?? bikes.first
    }
    private var unit: PressureUnit { profile.flatMap { PressureUnit(rawValue: $0.unitRaw) } ?? .psi }
    private var garageTemp: Double { profile?.garageTempC ?? 20 }
    private var advice: RideAdvice? { makeAdvice() }

    var body: some View {
        let palette = palette
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 8) {
                    ScreenTitle(text: "Presión de hoy", detail: "Un punto de partida para cada rueda. Afínalo de a medio psi en el sendero.")
                    SectionLabel(text: "Peso de hoy con equipo")
                    NumberField(title: "Kilogramos", text: $riderWeight, suffix: "kg")
                    SectionLabel(text: "Bici")
                    bikeSection
                    SectionLabel(text: "Modalidad")
                    Segmented(options: Discipline.allCases, selection: discipline, label: { $0.label }) { discipline = $0 }
                    SectionLabel(text: "Terreno")
                    Segmented(options: Terrain.allCases, selection: terrain, label: { $0.label }) { terrain = $0 }
                    SectionLabel(text: "Lugar y temperatura")
                    NumberField(title: "Lugar", text: $placeName, suffix: nil)
                    NumberField(title: "Temperatura", text: $temperature, suffix: "°C")
                    Button(loadingWeather ? "Buscando temperatura…" : "Usar mi ubicación") { loadWeather() }
                        .buttonStyle(LimeButtonStyle(enabled: !loadingWeather))
                        .disabled(loadingWeather)
                    resultSection
                    Text("Respeta el máximo marcado en la cubierta y en la llanta. La estatura y la edad del perfil no cambian esta cifra.")
                        .font(AppFont.text(13))
                        .foregroundStyle(palette.muted)
                        .padding(.top, 8)
                    if let message {
                        Text(message).font(AppFont.text(14, weight: .semibold)).foregroundStyle(palette.accent)
                        Button("Ocultar") { self.message = nil }.font(AppFont.text(14, weight: .semibold))
                    }
                }
                .padding(20)
            }
            .background(palette.bg)
            .navigationBarTitleDisplayMode(.inline)
        }
        .task(id: profile?.recordId) { seedWeight() }
        .sheet(isPresented: $saveOpen) { saveSheet }
    }

    @ViewBuilder
    private var bikeSection: some View {
        if bikes.isEmpty {
            Text("Todavía no hay bicis guardadas. El cálculo usa una 29 de \(Int(PressureCalculator.refBikeKg)) kg con cubiertas 2,40\" tubeless.")
                .font(AppFont.text(14))
                .foregroundStyle(palette.muted)
        } else if let bike = selectedBike {
            Segmented(options: bikes.map(\.recordId), selection: bike.recordId, label: { id in
                bikes.first { $0.recordId == id }?.name ?? ""
            }) { selectedBikeId = $0 }
            Text("\(bike.toDomain().wheelSize.label) · \(PressureCalculator.formatKg(bike.weightKg)) kg · del. \(bike.frontWidthIn)″ \(TireSystem(rawValue: bike.frontSystemRaw)?.label ?? "") · tras. \(bike.rearWidthIn)″ \(TireSystem(rawValue: bike.rearSystemRaw)?.label ?? "")")
                .font(AppFont.text(13))
                .foregroundStyle(palette.muted)
        }
    }

    @ViewBuilder
    private var resultSection: some View {
        if let advice {
            let bike = selectedBike?.toDomain()
            let frontWheel = bike?.front ?? PressureCalculator.defaultWheel
            let rearWheel = bike?.rear ?? PressureCalculator.defaultWheel
            HStack(alignment: .top, spacing: 10) {
                wheelCard("Delantera", advice.front, frontWheel)
                wheelCard("Trasera", advice.rear, rearWheel)
            }
            .padding(.top, 8)
            if let current = parseDecimal(temperature), abs(current - garageTemp) >= 0.05 {
                Text("Si inflas en casa a \(PressureCalculator.formatKg(garageTemp)) °C, pon \(PressureCalculator.format(advice.frontGaragePsi, unit: unit)) delante y \(PressureCalculator.format(advice.rearGaragePsi, unit: unit)) detrás.")
                    .font(AppFont.text(14))
                    .foregroundStyle(palette.ink)
            }
            Button("Guardar esta salida") {
                frontUsed = PressureCalculator.format(advice.front.psi, unit: unit)
                rearUsed = PressureCalculator.format(advice.rear.psi, unit: unit)
                saveOpen = true
            }
            .buttonStyle(LimeButtonStyle())
        } else {
            Text("Escribe el peso para ver la presión.")
                .font(AppFont.text(14))
                .foregroundStyle(palette.muted)
                .padding(.top, 12)
        }
    }

    private func wheelCard(_ title: String, _ advice: WheelAdvice, _ wheel: WheelSetup) -> some View {
        let range = PressureCalculator.displayRangePsi(advice, wheel: wheel)
        let rangeText = unit == .psi
            ? "\(PressureCalculator.formatPsi(range.0))–\(PressureCalculator.formatPsi(range.1)) psi"
            : "\(PressureCalculator.formatBar(range.0))–\(PressureCalculator.formatBar(range.1)) bar"
        let note: String? = advice.limitedByMax ? "Tope de cubierta o montaje." : (advice.raisedToMinimum ? "Mínimo de este montaje." : nil)
        return PressureCard(title: title, psi: advice.psi, unit: unit, range: rangeText, note: note)
    }

    private var saveSheet: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 12) {
                Text("Anota la presión que vas a usar. Empieza en la recomendada.")
                    .font(AppFont.text(15))
                    .foregroundStyle(palette.muted)
                NumberField(title: "Delantera", text: $frontUsed, suffix: unit.label)
                NumberField(title: "Trasera", text: $rearUsed, suffix: unit.label)
                Button("Guardar") { storeRide() }.buttonStyle(LimeButtonStyle())
                Spacer()
            }
            .padding(20)
            .background(palette.bg)
            .navigationTitle("Guardar salida")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancelar") { saveOpen = false }
                }
            }
        }
        .presentationDetents([.medium])
    }

    private func seedWeight() {
        guard !seeded, riderWeight.isEmpty, let profile, profile.weightKg > 0 else { return }
        riderWeight = PressureCalculator.formatKg(profile.weightKg)
        seeded = true
    }

    private func makeAdvice() -> RideAdvice? {
        guard let rider = parseDecimal(riderWeight), rider > 0, let temp = parseDecimal(temperature) else { return nil }
        let bike = selectedBike?.toDomain()
        let bikeWeight = bike?.weightKg ?? PressureCalculator.refBikeKg
        let front = bike?.front ?? PressureCalculator.defaultWheel
        let rear = bike?.rear ?? PressureCalculator.defaultWheel
        guard bikeWeight > 0, front.widthIn > 0, rear.widthIn > 0 else { return nil }
        let frontAdvice = PressureCalculator.recommend(PressureInput(riderWeightKg: rider, bikeWeightKg: bikeWeight, wheel: front, position: .front, discipline: discipline, terrain: terrain))
        let rearAdvice = PressureCalculator.recommend(PressureInput(riderWeightKg: rider, bikeWeightKg: bikeWeight, wheel: rear, position: .rear, discipline: discipline, terrain: terrain))
        return RideAdvice(
            front: frontAdvice,
            rear: rearAdvice,
            frontGaragePsi: PressureCalculator.garagePsi(trailPsi: frontAdvice.psi, trailTempC: temp, garageTempC: garageTemp),
            rearGaragePsi: PressureCalculator.garagePsi(trailPsi: rearAdvice.psi, trailTempC: temp, garageTempC: garageTemp)
        )
    }

    private func loadWeather() {
        loadingWeather = true
        message = nil
        Task {
            do {
                let place = try await TrailConditions.shared.current()
                temperature = String(format: "%.1f", locale: Locale(identifier: "es"), place.temperatureC)
                placeName = place.name
                latitude = place.latitude
                longitude = place.longitude
            } catch {
                message = error.localizedDescription
            }
            loadingWeather = false
        }
    }

    private func storeRide() {
        guard let advice, let front = parseDecimal(frontUsed), let rear = parseDecimal(rearUsed) else { return }
        let frontPsi = PressureCalculator.toPsi(front, unit: unit)
        let rearPsi = PressureCalculator.toPsi(rear, unit: unit)
        guard frontPsi > 0, rearPsi > 0, let rider = parseDecimal(riderWeight), let temp = parseDecimal(temperature) else { return }
        let bike = selectedBike?.toDomain()
        let now = AppTime.now()
        let log = PressureLog(
            id: AppTime.newId(),
            bikeId: bike?.id,
            bikeName: bike?.name ?? "Sin bici guardada",
            placeName: placeName.isEmpty ? "Sin lugar" : placeName,
            latitude: latitude,
            longitude: longitude,
            temperatureC: temp,
            garageTempC: garageTemp,
            riderWeightKg: rider,
            bikeWeightKg: bike?.weightKg ?? PressureCalculator.refBikeKg,
            discipline: discipline,
            terrain: terrain,
            frontRecommendedPsi: advice.front.psi,
            rearRecommendedPsi: advice.rear.psi,
            frontUsedPsi: frontPsi,
            rearUsedPsi: rearPsi,
            createdAt: now,
            updatedAt: now,
            deletedAt: nil
        )
        try? Library.save(log: log, in: context)
        message = "Salida guardada en el historial."
        saveOpen = false
    }
}
