package com.kzeta.presionmtb

import com.kzeta.presionmtb.domain.Bike
import com.kzeta.presionmtb.domain.Casing
import com.kzeta.presionmtb.domain.Discipline
import com.kzeta.presionmtb.domain.PressureLog
import com.kzeta.presionmtb.domain.PressureUnit
import com.kzeta.presionmtb.domain.RiderProfile
import com.kzeta.presionmtb.domain.SyncCodec
import com.kzeta.presionmtb.domain.SyncPayload
import com.kzeta.presionmtb.domain.Terrain
import com.kzeta.presionmtb.domain.TireSystem
import com.kzeta.presionmtb.domain.WheelSetup
import com.kzeta.presionmtb.domain.WheelSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SyncCodecTest {
    @Test
    fun roundTripKeepsTheDocument() {
        val payload = SyncPayload(
            schemaVersion = 1,
            exportedAt = "2026-11-16T18:00:00.000Z",
            profile = profile("2026-11-16T18:00:00Z"),
            bikes = listOf(bike("2026-11-16T18:00:00Z")),
            logs = listOf(log("2026-11-16T18:00:00Z")),
        )
        val parsed = SyncCodec.parse(SyncCodec.export(payload))
        assertEquals(payload.profile?.name, parsed.profile?.name)
        assertEquals(1, parsed.bikes.size)
        assertEquals(24.0, parsed.logs.first().rearUsedPsi, 0.0001)
        assertEquals(1, parsed.schemaVersion)
    }

    @Test
    fun newerWriteWinsAndTombstonesStay() {
        val localBike = bike("2026-11-16T18:00:00Z")
        val older = localBike.copy(name = "Vieja", updatedAt = "2026-11-16T12:00:00Z")
        val deleted = localBike.copy(
            deletedAt = "2026-11-16T19:00:00Z",
            updatedAt = "2026-11-16T19:00:00Z",
        )
        val kept = SyncCodec.merge(
            SyncPayload(1, "2026-11-16T18:00:00Z", null, listOf(localBike), emptyList()),
            SyncPayload(1, "2026-11-16T12:00:00Z", null, listOf(older), emptyList()),
        )
        assertEquals("Trail", kept.bikes.single().name)
        val gone = SyncCodec.merge(
            SyncPayload(1, "2026-11-16T18:00:00Z", null, listOf(localBike), emptyList()),
            SyncPayload(1, "2026-11-16T19:00:00Z", null, listOf(deleted), emptyList()),
        )
        assertEquals("2026-11-16T19:00:00Z", gone.bikes.single().deletedAt)
        val localProfile = profile("2026-11-16T20:00:00Z")
        val remoteProfile = profile("2026-11-16T10:00:00Z").copy(name = "Remoto")
        val profiles = SyncCodec.merge(
            SyncPayload(1, "2026-11-16T20:00:00Z", localProfile, emptyList(), emptyList()),
            SyncPayload(1, "2026-11-16T10:00:00Z", remoteProfile, emptyList(), emptyList()),
        )
        assertEquals("Ada", profiles.profile?.name)
        assertNull(
            SyncCodec.merge(
                SyncPayload(1, "2026-11-16T10:00:00Z", null, emptyList(), emptyList()),
                SyncPayload(1, "2026-11-16T10:00:00Z", null, emptyList(), emptyList()),
            ).profile,
        )
    }

    private fun profile(updated: String) = RiderProfile(
        id = "11111111-1111-1111-1111-111111111111",
        name = "Ada",
        weightKg = 68.0,
        heightCm = 170.0,
        age = 34,
        garageTempC = 20.0,
        unit = PressureUnit.PSI,
        createdAt = "2026-11-01T00:00:00Z",
        updatedAt = updated,
        deletedAt = null,
    )

    private fun bike(updated: String) = Bike(
        id = "22222222-2222-2222-2222-222222222222",
        name = "Trail",
        wheelSize = WheelSize.IN_29,
        weightKg = 12.5,
        front = WheelSetup(2.4, 30.0, TireSystem.TUBELESS, Casing.TRAIL, null),
        rear = WheelSetup(2.4, 30.0, TireSystem.TUBELESS, Casing.ENDURO, 35.0),
        createdAt = "2026-11-01T00:00:00Z",
        updatedAt = updated,
        deletedAt = null,
    )

    private fun log(updated: String) = PressureLog(
        id = "33333333-3333-3333-3333-333333333333",
        bikeId = "22222222-2222-2222-2222-222222222222",
        bikeName = "Trail",
        placeName = "La Angostura",
        latitude = -40.0,
        longitude = -71.0,
        temperatureC = 8.0,
        garageTempC = 20.0,
        riderWeightKg = 68.0,
        bikeWeightKg = 12.5,
        discipline = Discipline.TRAIL,
        terrain = Terrain.ROCK,
        frontRecommendedPsi = 21.5,
        rearRecommendedPsi = 24.0,
        frontUsedPsi = 22.0,
        rearUsedPsi = 24.0,
        createdAt = updated,
        updatedAt = updated,
        deletedAt = null,
    )
}
