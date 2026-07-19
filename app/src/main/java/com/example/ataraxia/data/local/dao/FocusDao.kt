package com.example.ataraxia.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ataraxia.data.local.entity.FocusSessionEntity
import com.example.ataraxia.data.local.entity.FocusIntentionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusDao {
    // Focus Session Logs Queries
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT SUM(durationMinutes) FROM focus_sessions")
    fun getTotalFocusDurationFlow(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Query("DELETE FROM focus_sessions")
    suspend fun clearAllSessions(): Int

    @Query("DELETE FROM focus_sessions WHERE id = :id")
    suspend fun deleteSession(id: Long): Int

    // Focus Intentions Queries
    @Query("SELECT * FROM focus_intentions ORDER BY name ASC")
    fun getAllCustomIntentions(): Flow<List<FocusIntentionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntention(intention: FocusIntentionEntity)

    @Query("DELETE FROM focus_intentions WHERE name = :name")
    suspend fun deleteIntention(name: String): Int
}
