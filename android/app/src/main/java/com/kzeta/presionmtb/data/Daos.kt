package com.kzeta.presionmtb.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile WHERE deletedAt IS NULL ORDER BY updatedAt DESC LIMIT 1")
    fun observeActive(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profile ORDER BY updatedAt DESC LIMIT 1")
    suspend fun latest(): ProfileEntity?

    @Query("DELETE FROM profile")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ProfileEntity)
}

@Dao
interface BikeDao {
    @Query("SELECT * FROM bikes WHERE deletedAt IS NULL ORDER BY name COLLATE NOCASE")
    fun observeActive(): Flow<List<BikeEntity>>

    @Query("SELECT * FROM bikes")
    suspend fun all(): List<BikeEntity>

    @Query("SELECT * FROM bikes WHERE id = :id AND deletedAt IS NULL LIMIT 1")
    suspend fun getActive(id: String): BikeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: BikeEntity)
}

@Dao
interface LogDao {
    @Query("SELECT * FROM logs WHERE deletedAt IS NULL ORDER BY createdAt DESC")
    fun observeActive(): Flow<List<LogEntity>>

    @Query("SELECT * FROM logs")
    suspend fun all(): List<LogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LogEntity)
}
