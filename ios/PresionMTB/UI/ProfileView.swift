import SwiftData
import SwiftUI
import UniformTypeIdentifiers

struct ProfileView: View {
    @Environment(\.modelContext) private var context
    @Environment(\.colorScheme) private var scheme
    @Query private var profiles: [RiderProfileRecord]
    @State private var recordId: String?
    @State private var createdAt: String?
    @State private var name = ""
    @State private var weight = ""
    @State private var height = ""
    @State private var age = ""
    @State private var garage = "20"
    @State private var unit = PressureUnit.psi
    @State private var message: String?
    @State private var error: String?
    @State private var exportOpen = false
    @State private var importOpen = false
    @State private var exportData = Data()
    private var palette: Palette { Palette.current(scheme) }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 8) {
                    ScreenTitle(text: "Perfil", detail: "El cálculo usa el peso. La estatura y la edad se guardan con tu perfil.")
                    NumberField(title: "Nombre", text: $name, suffix: nil)
                    NumberField(title: "Peso con equipo", text: $weight, suffix: "kg")
                    NumberField(title: "Estatura", text: $height, suffix: "cm")
                    NumberField(title: "Edad", text: $age, suffix: nil)
                    NumberField(title: "Temperatura del garaje", text: $garage, suffix: "°C")
                    SectionLabel(text: "Unidad principal")
                    Segmented(options: PressureUnit.allCases, selection: unit, label: { $0.label }) { unit = $0 }
                    if let error { Text(error).foregroundStyle(palette.accent).font(AppFont.text(14, weight: .semibold)) }
                    if let message { Text(message).foregroundStyle(palette.accent).font(AppFont.text(14, weight: .semibold)) }
                    Button("Guardar perfil") { save() }.buttonStyle(LimeButtonStyle())
                    Button("Exportar datos") { prepareExport() }.font(AppFont.text(16, weight: .semibold)).frame(maxWidth: .infinity)
                    Button("Importar datos") { importOpen = true }.font(AppFont.text(16, weight: .semibold)).frame(maxWidth: .infinity).padding(.bottom, 24)
                }
                .padding(20)
            }
            .background(palette.bg)
        }
        .onChange(of: profiles.map(\.recordId).sorted().joined(separator: ","), initial: true) { _, _ in
            guard recordId == nil,
                  let profile = profiles.filter({ $0.deletedAt == nil }).max(by: { $0.updatedAt < $1.updatedAt })
            else { return }
            apply(profile.toDomain())
        }
        .fileExporter(isPresented: $exportOpen, document: JsonFile(data: exportData), contentType: .json, defaultFilename: "presion-mtb") { result in
            if case .failure = result { error = "No se pudo exportar." }
            else { message = "Archivo exportado."; error = nil }
        }
        .fileImporter(isPresented: $importOpen, allowedContentTypes: [.json]) { result in
            switch result {
            case .success(let url):
                importFile(url)
            case .failure:
                error = "No se pudo importar."
            }
        }
    }

    private func apply(_ profile: RiderProfile) {
        recordId = profile.id
        createdAt = profile.createdAt
        name = profile.name
        weight = profile.weightKg > 0 ? PressureCalculator.formatKg(profile.weightKg) : ""
        height = profile.heightCm > 0 ? PressureCalculator.formatKg(profile.heightCm) : ""
        age = profile.age > 0 ? String(profile.age) : ""
        garage = PressureCalculator.formatKg(profile.garageTempC)
        unit = profile.unit
    }

    private func save() {
        let weightKg = parseDecimal(weight)
        let heightCm = height.trimmingCharacters(in: .whitespaces).isEmpty ? nil : parseDecimal(height)
        let ageValue = age.trimmingCharacters(in: .whitespaces).isEmpty ? nil : Int(age)
        let garageTemp = parseDecimal(garage)
        if weightKg == nil || !(20...250).contains(weightKg ?? 0) {
            error = "El peso tiene que estar entre 20 y 250 kg."
            return
        }
        if let heightCm, !(100...230).contains(heightCm) {
            error = "La estatura tiene que estar entre 100 y 230 cm."
            return
        }
        if let ageValue, !(5...100).contains(ageValue) {
            error = "La edad tiene que estar entre 5 y 100."
            return
        }
        if garageTemp == nil || !(-30...50).contains(garageTemp ?? 0) {
            error = "Revisa la temperatura del garaje."
            return
        }
        let now = AppTime.now()
        let profile = RiderProfile(
            id: recordId ?? AppTime.newId(),
            name: name.trimmingCharacters(in: .whitespaces),
            weightKg: weightKg ?? 0,
            heightCm: heightCm ?? 0,
            age: ageValue ?? 0,
            garageTempC: garageTemp ?? 20,
            unit: unit,
            createdAt: createdAt ?? now,
            updatedAt: now,
            deletedAt: nil
        )
        do {
            try Library.save(profile: profile, in: context)
            apply(profile)
            message = "Perfil guardado."
            error = nil
        } catch {
            self.error = "No se pudo guardar el perfil."
        }
    }

    private func prepareExport() {
        do {
            exportData = try Library.exportData(in: context)
            exportOpen = true
        } catch {
            self.error = "No se pudo exportar."
        }
    }

    private func importFile(_ url: URL) {
        let access = url.startAccessingSecurityScopedResource()
        defer { if access { url.stopAccessingSecurityScopedResource() } }
        do {
            let data = try Data(contentsOf: url)
            try Library.importData(data, in: context)
            if let profile = Library.profile(in: context)?.toDomain() { apply(profile) }
            message = "Datos importados."
            error = nil
        } catch {
            self.error = error.localizedDescription
        }
    }
}

struct JsonFile: FileDocument {
    static var readableContentTypes: [UTType] { [.json] }
    var data: Data
    init(data: Data) { self.data = data }
    init(configuration: ReadConfiguration) throws {
        data = configuration.file.regularFileContents ?? Data()
    }
    func fileWrapper(configuration: WriteConfiguration) throws -> FileWrapper {
        FileWrapper(regularFileWithContents: data)
    }
}
