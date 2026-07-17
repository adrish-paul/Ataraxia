package com.example.ataraxia.features.journal.data

import com.example.ataraxia.data.local.dao.JournalDao
import com.example.ataraxia.data.local.entity.JournalEntryEntity
import com.example.ataraxia.features.journal.domain.JournalRepository
import kotlinx.coroutines.flow.Flow

class JournalRepositoryImpl(
    private val journalDao: JournalDao
) : JournalRepository {
    override fun getAllEntries(): Flow<List<JournalEntryEntity>> = journalDao.getAllEntries()
    override suspend fun insertEntry(entry: JournalEntryEntity) {
        journalDao.insertEntry(entry)
    }
    override suspend fun deleteEntry(id: Long) {
        journalDao.deleteEntry(id)
    }
    override suspend fun toggleFavorite(id: Long) {
        journalDao.toggleFavorite(id)
    }
}
