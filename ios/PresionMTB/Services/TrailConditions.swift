import CoreLocation
import Foundation

struct TrailPlace {
    var name: String
    var temperatureC: Double
    var latitude: Double
    var longitude: Double
}

struct TrailException: LocalizedError {
    var message: String
    var errorDescription: String? { message }
}

@MainActor
final class TrailConditions: NSObject, CLLocationManagerDelegate {
    static let shared = TrailConditions()
    private let manager = CLLocationManager()
    private var continuation: CheckedContinuation<CLLocation, Error>?

    func current() async throws -> TrailPlace {
        let location = try await requestLocation()
        let name = await placeName(for: location)
        let temperature = try await Self.temperature(latitude: location.coordinate.latitude, longitude: location.coordinate.longitude)
        return TrailPlace(
            name: name,
            temperatureC: temperature,
            latitude: location.coordinate.latitude,
            longitude: location.coordinate.longitude
        )
    }

    private func requestLocation() async throws -> CLLocation {
        try await withCheckedThrowingContinuation { continuation in
            self.continuation = continuation
            manager.delegate = self
            switch manager.authorizationStatus {
            case .authorizedAlways, .authorizedWhenInUse:
                manager.requestLocation()
            case .notDetermined:
                manager.requestWhenInUseAuthorization()
            default:
                finish(TrailException(message: "Puedes escribir la temperatura y el lugar a mano."))
            }
        }
    }

    nonisolated func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        Task { @MainActor in
            switch manager.authorizationStatus {
            case .authorizedAlways, .authorizedWhenInUse:
                manager.requestLocation()
            case .denied, .restricted:
                finish(TrailException(message: "Puedes escribir la temperatura y el lugar a mano."))
            default:
                break
            }
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        Task { @MainActor in
            guard let location = locations.first else { return }
            let pending = continuation
            continuation = nil
            pending?.resume(returning: location)
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        Task { @MainActor in
            finish(TrailException(message: "No encontré una posición. Escribe la temperatura a mano."))
        }
    }

    private func finish(_ error: Error) {
        let pending = continuation
        continuation = nil
        pending?.resume(throwing: error)
    }

    private func placeName(for location: CLLocation) async -> String {
        let geocoder = CLGeocoder()
        guard let mark = try? await geocoder.reverseGeocodeLocation(location).first else {
            return "Ubicación actual"
        }
        let locality = mark.locality ?? mark.subAdministrativeArea ?? mark.name
        let parts = [locality, mark.administrativeArea].compactMap { $0 }
        let unique = Array(NSOrderedSet(array: parts)) as? [String] ?? parts
        let name = unique.joined(separator: ", ")
        return name.isEmpty ? "Ubicación actual" : name
    }

    private static func temperature(latitude: Double, longitude: Double) async throws -> Double {
        let lat = String(format: "%.5f", locale: Locale(identifier: "en_US_POSIX"), latitude)
        let lon = String(format: "%.5f", locale: Locale(identifier: "en_US_POSIX"), longitude)
        guard let url = URL(string: "https://api.open-meteo.com/v1/forecast?latitude=\(lat)&longitude=\(lon)&current=temperature_2m") else {
            throw TrailException(message: "El servicio de clima no respondió.")
        }
        var request = URLRequest(url: url, timeoutInterval: 12)
        request.setValue("PresionMTB/1.0 (com.kzeta.presionmtb)", forHTTPHeaderField: "User-Agent")
        do {
            let (data, response) = try await URLSession.shared.data(for: request)
            guard let http = response as? HTTPURLResponse, http.statusCode == 200 else {
                throw TrailException(message: "El servicio de clima no respondió.")
            }
            return try JSONDecoder().decode(MeteoResponse.self, from: data).current.temperature_2m
        } catch let error as TrailException {
            throw error
        } catch {
            throw TrailException(message: "Sin conexión para consultar la temperatura.")
        }
    }
}

private struct MeteoResponse: Decodable {
    struct Current: Decodable { let temperature_2m: Double }
    let current: Current
}
