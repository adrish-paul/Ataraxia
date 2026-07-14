package com.example.ataraxia.data.repository

import com.example.ataraxia.data.local.dao.JournalDao
import com.example.ataraxia.data.local.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

class JournalRepository(private val journalDao: JournalDao) {

    val allEntries: Flow<List<JournalEntryEntity>> = journalDao.getAllEntries()

    suspend fun insert(entry: JournalEntryEntity): Long {
        return journalDao.insertEntry(entry)
    }

    suspend fun update(entry: JournalEntryEntity) {
        journalDao.updateEntry(entry)
    }

    suspend fun delete(id: Long) {
        journalDao.deleteEntry(id)
    }

    suspend fun clearAll() {
        journalDao.clearAllEntries()
    }
}
