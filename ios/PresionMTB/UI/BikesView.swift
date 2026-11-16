import SwiftData
import SwiftUI

struct BikesView: View {
    @Environment(\.colorScheme) private var scheme
    @Query private var stored: [BikeRecord]
    private var bikes: [BikeRecord] {
        stored.filter { $0.deletedAt == nil }.sorted { $0.name.localizedCaseInsensitiveCompare($1.name) == .orderedAscending }
    }
    private var palette: Palette { Palette.current(scheme) }

    var body: some View {
        NavigationStack {
            Group {
                if bikes.isEmpty {
                    VStack(alignment: .leading, spacing: 8) {
                        ScreenTitle(text: "Bicis", detail: "Guarda cada bici con el aro, el ancho de cubierta y el montaje. La calculadora las usa tal cual.")
                        Spacer()
                    }
                    .padding(20)
                } else {
                    ScrollView {
                        VStack(spacing: 10) {
                            ScreenTitle(text: "Bicis", detail: "Toca una bici para cambiar el montaje.")
                            ForEach(bikes, id: \.recordId) { bike in
                                NavigationLink(value: bike.recordId) {
                                    bikeCard(bike)
                                }
                                .buttonStyle(.plain)
                            }
                        }
                        .padding(20)
                    }
                }
            }
            .background(palette.bg)
            .navigationDestination(for: String.self) { bikeId in
                BikeEditView(bikeId: bikeId)
            }
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    NavigationLink(value: "new") {
                        Image(systemName: "plus")
                            .foregroundStyle(AppColor.onLime)
                            .padding(8)
                            .background(AppColor.lime)
                            .clipShape(Circle())
                    }
                }
            }
        }
    }

    private func bikeCard(_ bike: BikeRecord) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(bike.name).font(AppFont.text(18, weight: .bold)).foregroundStyle(palette.ink)
            Text("\(bike.toDomain().wheelSize.label) · \(PressureCalculator.formatKg(bike.weightKg)) kg")
                .font(AppFont.text(14)).foregroundStyle(palette.muted)
            Text("Del. \(bike.frontWidthIn)″ / \(Int(bike.frontRimInternalMm)) mm · \(TireSystem(rawValue: bike.frontSystemRaw)?.label ?? "")")
                .font(AppFont.text(14)).foregroundStyle(palette.ink)
            Text("Tras. \(bike.rearWidthIn)″ / \(Int(bike.rearRimInternalMm)) mm · \(TireSystem(rawValue: bike.rearSystemRaw)?.label ?? "")")
                .font(AppFont.text(14)).foregroundStyle(palette.ink)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(palette.surface)
        .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: 20, style: .continuous).stroke(palette.line, lineWidth: 1))
    }
}

struct BikeEditView: View {
    var bikeId: String
    @Environment(\.modelContext) private var context
    @Environment(\.dismiss) private var dismiss
    @Environment(\.colorScheme) private var scheme
    @State private var name = ""
    @State private var wheelSize = WheelSize.in29
    @State private var weight = "12"
    @State private var front = WheelForm()
    @State private var rear = WheelForm()
    @State private var createdAt: String?
    @State private var error: String?
    @State private var loaded = false
    private var palette: Palette { Palette.current(scheme) }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 8) {
                NumberField(title: "Nombre", text: $name, suffix: nil)
                SectionLabel(text: "Medida de rueda")
                Segmented(options: WheelSize.allCases, selection: wheelSize, label: { $0.label }) { wheelSize = $0 }
                NumberField(title: "Peso de la bici", text: $weight, suffix: "kg")
                wheelEditor("Rueda delantera", form: $front)
                wheelEditor("Rueda trasera", form: $rear)
                if let error {
                    Text(error).foregroundStyle(palette.accent).font(AppFont.text(14, weight: .semibold))
                }
                Button("Guardar bici") { save() }.buttonStyle(LimeButtonStyle())
                if bikeId != "new" {
                    Button("Eliminar bici") { remove() }
                        .font(AppFont.text(15, weight: .semibold))
                        .foregroundStyle(palette.accent)
                        .frame(maxWidth: .infinity)
                        .padding(.bottom, 24)
                }
            }
            .padding(20)
        }
        .background(palette.bg)
        .navigationTitle(bikeId == "new" ? "Nueva bici" : "Editar bici")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            guard !loaded, bikeId != "new", let bike = Library.bike(id: bikeId, in: context) else {
                loaded = true
                return
            }
            let domain = bike.toDomain()
            name = domain.name
            wheelSize = domain.wheelSize
            weight = PressureCalculator.formatKg(domain.weightKg)
            front = WheelForm(domain.front)
            rear = WheelForm(domain.rear)
            createdAt = domain.createdAt
            loaded = true
        }
    }

    private func wheelEditor(_ title: String, form: Binding<WheelForm>) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionLabel(text: title)
            NumberField(title: "Ancho de cubierta", text: form.width, suffix: "pulgadas")
            NumberField(title: "Ancho interno de llanta", text: form.rim, suffix: "mm")
            NumberField(title: "Máximo de la cubierta", text: form.maxPsi, suffix: "psi")
            SectionLabel(text: "Montaje")
            Segmented(options: TireSystem.allCases, selection: form.wrappedValue.system, label: { $0.label }) { form.wrappedValue.system = $0 }
            SectionLabel(text: "Carcasa")
            Segmented(options: Casing.allCases, selection: form.wrappedValue.casing, label: { $0.label }) { form.wrappedValue.casing = $0 }
        }
    }

    private func save() {
        guard let weightKg = parseDecimal(weight), (4...45).contains(weightKg), !name.trimmingCharacters(in: .whitespaces).isEmpty,
              let frontSetup = front.setup, let rearSetup = rear.setup else {
            error = "Revisa el nombre, el peso y las medidas de cada rueda."
            return
        }
        let now = AppTime.now()
        let bike = Bike(
            id: bikeId == "new" ? AppTime.newId() : bikeId,
            name: name.trimmingCharacters(in: .whitespaces),
            wheelSize: wheelSize,
            weightKg: weightKg,
            front: frontSetup,
            rear: rearSetup,
            createdAt: createdAt ?? now,
            updatedAt: now,
            deletedAt: nil
        )
        do {
            try Library.save(bike: bike, in: context)
            dismiss()
        } catch {
            self.error = "No se pudo guardar la bici."
        }
    }

    private func remove() {
        guard let record = Library.bike(id: bikeId, in: context) else { return }
        try? Library.softDelete(record, in: context)
        dismiss()
    }
}

struct WheelForm {
    var width = "2,4"
    var rim = "30"
    var maxPsi = ""
    var system = TireSystem.tubeless
    var casing = Casing.trail

    init() {}

    init(_ setup: WheelSetup) {
        width = formatMeasure(setup.widthIn)
        rim = formatMeasure(setup.rimInternalMm)
        if let max = setup.maxPsi { maxPsi = formatMeasure(max) }
        system = setup.system
        casing = setup.casing
    }

    var setup: WheelSetup? {
        guard let widthIn = parseDecimal(width), let rimMm = parseDecimal(rim), (1.5...4).contains(widthIn), (15...50).contains(rimMm) else { return nil }
        var max: Double?
        if !maxPsi.trimmingCharacters(in: .whitespaces).isEmpty {
            guard let value = parseDecimal(maxPsi), (8...80).contains(value) else { return nil }
            max = value
        }
        return WheelSetup(widthIn: widthIn, rimInternalMm: rimMm, system: system, casing: casing, maxPsi: max)
    }
}

private func formatMeasure(_ value: Double) -> String {
    value.truncatingRemainder(dividingBy: 1) == 0 ? String(Int(value)) : String(format: "%.1f", locale: Locale(identifier: "es"), value)
}
