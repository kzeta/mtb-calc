import SwiftUI

struct SectionLabel: View {
    var text: String
    @Environment(\.colorScheme) private var scheme
    var body: some View {
        Text(text)
            .font(AppFont.text(13, weight: .semibold))
            .foregroundStyle(Palette.current(scheme).muted)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.top, 14)
    }
}

struct Segmented<T: Hashable>: View {
    var options: [T]
    var selection: T
    var label: (T) -> String
    var onSelect: (T) -> Void
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        let palette = Palette.current(scheme)
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(options, id: \.self) { option in
                    let active = option == selection
                    Button(label(option)) { onSelect(option) }
                        .buttonStyle(.plain)
                        .font(AppFont.text(14, weight: .semibold))
                        .foregroundStyle(active ? AppColor.onLime : palette.ink)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 8)
                        .background(active ? AppColor.lime : palette.surfaceHigh)
                        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                        .overlay(RoundedRectangle(cornerRadius: 14, style: .continuous).stroke(active ? AppColor.lime : palette.line, lineWidth: 1))
                }
            }
        }
    }
}

struct NumberField: View {
    var title: String
    @Binding var text: String
    var suffix: String?
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        let palette = Palette.current(scheme)
        HStack {
            TextField(title, text: $text)
                .font(AppFont.text(16))
                .textFieldStyle(.plain)
            if let suffix {
                Text(suffix).font(AppFont.text(14, weight: .semibold)).foregroundStyle(palette.muted)
            }
        }
        .padding(14)
        .background(palette.surface)
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: 14, style: .continuous).stroke(palette.line, lineWidth: 1))
    }
}

struct PressureCard: View {
    var title: String
    var psi: Double
    var unit: PressureUnit
    var range: String
    var note: String?
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        let palette = Palette.current(scheme)
        let primary = PressureCalculator.format(psi, unit: unit)
        let secondary = unit == .psi
            ? "\(PressureCalculator.formatBar(psi)) bar"
            : "\(PressureCalculator.formatPsi(psi)) psi"
        VStack(alignment: .leading, spacing: 2) {
            Text(title).font(AppFont.text(12, weight: .semibold)).foregroundStyle(palette.muted)
            Text(primary)
                .font(AppFont.text(44, weight: .bold))
                .foregroundStyle(palette.isDark ? AppColor.lime : palette.ink)
                .minimumScaleFactor(0.6)
                .lineLimit(1)
            Text(unit.label).font(AppFont.text(14, weight: .semibold)).foregroundStyle(palette.accent)
            Text(secondary).font(AppFont.text(13)).foregroundStyle(palette.muted)
            Text(range).font(AppFont.text(13)).foregroundStyle(palette.ink).padding(.top, 6)
            if let note {
                Text(note).font(AppFont.text(12, weight: .semibold)).foregroundStyle(palette.accent)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(palette.surface)
        .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: 20, style: .continuous).stroke(palette.line, lineWidth: 1))
    }
}

struct ScreenTitle: View {
    var text: String
    var detail: String
    @Environment(\.colorScheme) private var scheme
    var body: some View {
        let palette = Palette.current(scheme)
        VStack(alignment: .leading, spacing: 6) {
            Text(text).font(AppFont.text(28, weight: .bold)).foregroundStyle(palette.ink)
            Text(detail).font(AppFont.text(15)).foregroundStyle(palette.muted)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}
