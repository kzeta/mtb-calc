import SwiftData
import SwiftUI

struct HistoryView: View {
    @Environment(\.modelContext) private var context
    @Environment(\.colorScheme) private var scheme
    @Query private var stored: [PressureLogRecord]
    @Query private var profiles: [RiderProfileRecord]
    @State private var pending: PressureLogRecord?
    private var logs: [PressureLogRecord] {
        stored.filter { $0.deletedAt == nil }.sorted { $0.createdAt > $1.createdAt }
    }
    private var unit: PressureUnit {
        let profile = profiles.filter { $0.deletedAt == nil }.max { $0.updatedAt < $1.updatedAt }
        return profile.flatMap { PressureUnit(rawValue: $0.unitRaw) } ?? .psi
    }
    private var palette: Palette { Palette.current(scheme) }

    var body: some View {
        NavigationStack {
            Group {
                if logs.isEmpty {
                    VStack(alignment: .leading, spacing: 8) {
                        ScreenTitle(text: "Historial", detail: "Cuando guardes una salida vas a ver el lugar, la temperatura y la presión que usaste.")
                        Spacer()
                    }
                    .padding(20)
                } else {
                    ScrollView {
                        VStack(spacing: 10) {
                            ScreenTitle(text: "Historial", detail: "Cada salida queda en el teléfono, con la temperatura de ese día.")
                            ForEach(logs, id: \.recordId) { log in
                                logCard(log)
                            }
                        }
                        .padding(20)
                    }
                }
            }
            .background(palette.bg)
        }
        .confirmationDialog("Eliminar salida", isPresented: Binding(get: { pending != nil }, set: { if !$0 { pending = nil } })) {
            Button("Eliminar", role: .destructive) {
                if let pending { try? Library.softDelete(pending, in: context) }
                pending = nil
            }
            Button("Cancelar", role: .cancel) { pending = nil }
        } message: {
            Text("Se oculta del historial y queda marcada por si más adelante sincronizas.")
        }
    }

    private func logCard(_ log: PressureLogRecord) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(log.placeName).font(AppFont.text(18, weight: .bold)).foregroundStyle(palette.ink)
            Text(formatWhen(log.createdAt)).font(AppFont.text(13)).foregroundStyle(palette.muted)
            Text("\(log.bikeName) · \(PressureCalculator.formatKg(log.temperatureC)) °C").font(AppFont.text(14)).foregroundStyle(palette.ink)
            Text("Usada \(PressureCalculator.format(log.frontUsedPsi, unit: unit)) / \(PressureCalculator.format(log.rearUsedPsi, unit: unit)) \(unit.label)")
                .font(AppFont.text(15, weight: .semibold))
                .foregroundStyle(palette.isDark ? AppColor.lime : palette.accent)
            Text("Recomendada \(PressureCalculator.format(log.frontRecommendedPsi, unit: unit)) / \(PressureCalculator.format(log.rearRecommendedPsi, unit: unit)) \(unit.label)")
                .font(AppFont.text(13)).foregroundStyle(palette.muted)
            Button("Eliminar") { pending = log }
                .font(AppFont.text(14, weight: .semibold))
                .foregroundStyle(palette.accent)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(palette.surface)
        .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: 20, style: .continuous).stroke(palette.line, lineWidth: 1))
    }
}

private func formatWhen(_ iso: String) -> String {
    let date = AppTime.parse(iso)
    let formatter = DateFormatter()
    formatter.locale = Locale(identifier: "es")
    formatter.dateStyle = .medium
    formatter.timeStyle = .short
    return formatter.string(from: date)
}
