package com.example.ataraxia.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.ataraxia.data.local.dao.BreatheDao
import com.example.ataraxia.data.local.dao.FocusDao
import com.example.ataraxia.data.local.dao.JournalDao
import com.example.ataraxia.data.local.dao.MoodLogDao
import com.example.ataraxia.data.local.entity.BreatheSessionEntity
import com.example.ataraxia.data.local.entity.FocusSessionEntity
import com.example.ataraxia.data.local.entity.JournalEntryEntity
import com.example.ataraxia.data.local.entity.MoodLogEntity

import com.example.ataraxia.data.local.entity.FocusIntentionEntity

@Database(
    entities = [
        JournalEntryEntity::class,
        BreatheSessionEntity::class,
        FocusSessionEntity::class,
        MoodLogEntity::class,
        FocusIntentionEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AtaraxiaDatabase : RoomDatabase() {

    abstract fun journalDao(): JournalDao
    abstract fun breatheDao(): BreatheDao
    abstract fun focusDao(): FocusDao
    abstract fun moodLogDao(): MoodLogDao

    companion object {
        @Volatile
        private var INSTANCE: AtaraxiaDatabase? = null

        fun getDatabase(context: Context): AtaraxiaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AtaraxiaDatabase::class.java,
                    "ataraxia_database"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
