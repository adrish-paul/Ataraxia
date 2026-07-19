package com.example.ataraxia.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ataraxia.data.local.entity.MoodLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodLog(moodLog: MoodLogEntity)

    @Query("SELECT * FROM mood_logs WHERE dateStr = :dateStr LIMIT 1")
    suspend fun getMoodLogForDate(dateStr: String): MoodLogEntity?

    @Query("SELECT * FROM mood_logs ORDER BY dateStr DESC")
    fun getAllMoodLogsFlow(): Flow<List<MoodLogEntity>>

    @Query("DELETE FROM mood_logs")
    suspend fun clearAllMoodLogs(): Int
}
