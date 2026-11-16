import SwiftData
import SwiftUI

@main
struct PresionMTBApp: App {
    var body: some Scene {
        WindowGroup {
            RootTabView()
                .background(Color(red: 0.055, green: 0.067, blue: 0.063).ignoresSafeArea())
        }
        .modelContainer(for: [RiderProfileRecord.self, BikeRecord.self, PressureLogRecord.self])
    }
}
