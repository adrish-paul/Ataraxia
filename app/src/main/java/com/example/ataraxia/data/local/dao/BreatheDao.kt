package com.example.ataraxia.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ataraxia.data.local.entity.BreatheSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BreatheDao {
    @Query("SELECT * FROM breathe_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<BreatheSessionEntity>>

    @Query("SELECT SUM(durationSeconds) FROM breathe_sessions")
    fun getTotalBreatheDurationFlow(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: BreatheSessionEntity): Long

    @Query("DELETE FROM breathe_sessions WHERE id = :id")
    suspend fun deleteSession(id: Long)

    @Query("DELETE FROM breathe_sessions")
    suspend fun clearAllSessions(): Int
}
