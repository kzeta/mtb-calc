package com.kzeta.presionmtb.data

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.kzeta.presionmtb.domain.AppTime
import com.kzeta.presionmtb.domain.Bike
import com.kzeta.presionmtb.domain.PressureLog
import com.kzeta.presionmtb.domain.RiderProfile
import com.kzeta.presionmtb.domain.SyncCodec
import com.kzeta.presionmtb.domain.SyncPayload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LibraryRepository(
    private val context: Context,
    private val db: AppDatabase,
) {
    fun observeProfile(): Flow<RiderProfile?> = db.profileDao().observeActive().map { it?.toDomain() }

    fun observeBikes(): Flow<List<Bike>> = db.bikeDao().observeActive().map { rows -> rows.map { it.toDomain() } }

    fun observeLogs(): Flow<List<PressureLog>> = db.logDao().observeActive().map { rows -> rows.map { it.toDomain() } }

    suspend fun getProfile(): RiderProfile? = db.profileDao().latest()?.takeIf { it.deletedAt == null }?.toDomain()

    suspend fun getBike(id: String): Bike? = db.bikeDao().getActive(id)?.toDomain()

    suspend fun saveProfile(profile: RiderProfile) {
        db.withTransaction {
            db.profileDao().deleteAll()
            db.profileDao().upsert(profile.toEntity())
        }
    }

    suspend fun saveBike(bike: Bike) {
        db.bikeDao().upsert(bike.toEntity())
    }

    suspend fun saveLog(log: PressureLog) {
        db.logDao().upsert(log.toEntity())
    }

    suspend fun softDeleteBike(bike: Bike) {
        val now = AppTime.now()
        db.bikeDao().upsert(bike.copy(deletedAt = now, updatedAt = now).toEntity())
    }

    suspend fun softDeleteLog(log: PressureLog) {
        val now = AppTime.now()
        db.logDao().upsert(log.copy(deletedAt = now, updatedAt = now).toEntity())
    }

    suspend fun exportJson(): String = SyncCodec.export(snapshot())

    suspend fun writeExport(uri: Uri) {
        val json = exportJson()
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(json.toByteArray(Charsets.UTF_8))
        } ?: error("No se pudo crear el archivo.")
    }

    suspend fun importFrom(uri: Uri) {
        val json = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.readBytes().toString(Charsets.UTF_8)
        } ?: error("No se pudo leer el archivo.")
        importJson(json)
    }

    suspend fun importJson(json: String) {
        val incoming = SyncCodec.parse(json)
        val merged = SyncCodec.merge(snapshot(), incoming)
        db.withTransaction {
            db.profileDao().deleteAll()
            merged.profile?.let { db.profileDao().upsert(it.toEntity()) }
            merged.bikes.forEach { db.bikeDao().upsert(it.toEntity()) }
            merged.logs.forEach { db.logDao().upsert(it.toEntity()) }
        }
    }

    private suspend fun snapshot(): SyncPayload {
        val profile = db.profileDao().latest()?.toDomain()
        return SyncPayload(
            schemaVersion = 1,
            exportedAt = AppTime.now(),
            profile = profile,
            bikes = db.bikeDao().all().map { it.toDomain() },
            logs = db.logDao().all().map { it.toDomain() },
        )
    }
}
