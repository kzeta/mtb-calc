package com.kzeta.presionmtb.domain

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

object AppTime {
    fun now(): String = Instant.now().truncatedTo(ChronoUnit.MILLIS).toString()

    fun parse(value: String): Instant = Instant.parse(value)

    fun newId(): String = UUID.randomUUID().toString()
}

object SyncCodec {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = true
    }

    fun export(payload: SyncPayload): String = json.encodeToString(payload.normalized())

    fun parse(raw: String): SyncPayload {
        val payload = json.decodeFromString<SyncPayload>(raw)
        if (payload.schemaVersion != 1) {
            throw IllegalArgumentException("Este archivo es de una versión más nueva de la app.")
        }
        return payload.normalized()
    }

    fun merge(local: SyncPayload, incoming: SyncPayload): SyncPayload {
        return SyncPayload(
            schemaVersion = 1,
            exportedAt = AppTime.now(),
            profile = newer(local.profile, incoming.profile) { it.updatedAt },
            bikes = mergeLists(local.bikes, incoming.bikes, Bike::id, Bike::updatedAt),
            logs = mergeLists(local.logs, incoming.logs, PressureLog::id, PressureLog::updatedAt),
        )
    }

    private fun SyncPayload.normalized(): SyncPayload = copy(
        profile = profile?.copy(id = profile.id.lowercase()),
        bikes = bikes.map { it.copy(id = it.id.lowercase()) },
        logs = logs.map { log ->
            log.copy(id = log.id.lowercase(), bikeId = log.bikeId?.lowercase())
        },
    )

    private fun <T> newer(local: T?, incoming: T?, updatedAt: (T) -> String): T? {
        if (local == null) return incoming
        if (incoming == null) return local
        return if (AppTime.parse(updatedAt(incoming)) >= AppTime.parse(updatedAt(local))) {
            incoming
        } else {
            local
        }
    }

    private fun <T> mergeLists(
        local: List<T>,
        incoming: List<T>,
        id: (T) -> String,
        updatedAt: (T) -> String,
    ): List<T> {
        val merged = LinkedHashMap<String, T>()
        local.forEach { merged[id(it)] = it }
        incoming.forEach { item ->
            val current = merged[id(item)]
            if (current == null || AppTime.parse(updatedAt(item)) >= AppTime.parse(updatedAt(current))) {
                merged[id(item)] = item
            }
        }
        return merged.values.toList()
    }
}
