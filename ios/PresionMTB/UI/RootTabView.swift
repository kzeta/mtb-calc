import SwiftUI

struct RootTabView: View {
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        TabView {
            CalculatorView()
                .tabItem { Label("Calcular", systemImage: "speedometer") }
            BikesView()
                .tabItem { Label("Bicis", systemImage: "bicycle") }
            HistoryView()
                .tabItem { Label("Historial", systemImage: "clock") }
            ProfileView()
                .tabItem { Label("Perfil", systemImage: "person") }
        }
        .tint(scheme == .dark ? AppColor.lime : AppColor.lightAccent)
    }
}
