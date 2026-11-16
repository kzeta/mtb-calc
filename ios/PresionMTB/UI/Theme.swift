import SwiftUI

enum AppColor {
    static let inkBg = Color(red: 0.055, green: 0.067, blue: 0.063)
    static let inkSurface = Color(red: 0.090, green: 0.110, blue: 0.102)
    static let inkHigh = Color(red: 0.133, green: 0.157, blue: 0.149)
    static let inkText = Color(red: 0.957, green: 0.969, blue: 0.949)
    static let inkMuted = Color(red: 0.718, green: 0.753, blue: 0.714)
    static let lightBg = Color(red: 0.953, green: 0.961, blue: 0.937)
    static let lightSurface = Color.white
    static let lightHigh = Color(red: 0.906, green: 0.922, blue: 0.890)
    static let lightText = Color(red: 0.078, green: 0.094, blue: 0.086)
    static let lightMuted = Color(red: 0.361, green: 0.404, blue: 0.373)
    static let lime = Color(red: 0.776, green: 0.961, blue: 0.290)
    static let onLime = Color(red: 0.102, green: 0.133, blue: 0.031)
    static let lightAccent = Color(red: 0.306, green: 0.420, blue: 0.071)
}

struct Palette {
    var bg: Color
    var surface: Color
    var surfaceHigh: Color
    var ink: Color
    var muted: Color
    var accent: Color
    var line: Color
    var isDark: Bool

    static func current(_ scheme: ColorScheme) -> Palette {
        if scheme == .dark {
            Palette(bg: AppColor.inkBg, surface: AppColor.inkSurface, surfaceHigh: AppColor.inkHigh, ink: AppColor.inkText, muted: AppColor.inkMuted, accent: AppColor.lime, line: Color(red: 0.227, green: 0.263, blue: 0.235), isDark: true)
        } else {
            Palette(bg: AppColor.lightBg, surface: AppColor.lightSurface, surfaceHigh: AppColor.lightHigh, ink: AppColor.lightText, muted: AppColor.lightMuted, accent: AppColor.lightAccent, line: Color(red: 0.835, green: 0.859, blue: 0.816), isDark: false)
        }
    }
}

enum AppFont {
    static func text(_ size: CGFloat, weight: Font.Weight = .regular) -> Font {
        let name: String
        switch weight {
        case .bold: name = "Manrope-Bold"
        case .semibold: name = "Manrope-SemiBold"
        case .medium: name = "Manrope-Medium"
        default: name = "Manrope-Regular"
        }
        return .custom(name, size: size)
    }
}

struct LimeButtonStyle: ButtonStyle {
    var enabled: Bool = true
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(AppFont.text(16, weight: .bold))
            .foregroundStyle(enabled ? AppColor.onLime : AppColor.inkMuted)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .background(enabled ? AppColor.lime : AppColor.inkHigh)
            .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
            .opacity(configuration.isPressed ? 0.85 : 1)
    }
}
