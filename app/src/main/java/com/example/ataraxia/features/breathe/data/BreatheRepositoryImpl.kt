package com.example.ataraxia.features.breathe.data

import com.example.ataraxia.data.local.dao.BreatheDao
import com.example.ataraxia.data.local.entity.BreatheSessionEntity
import com.example.ataraxia.features.breathe.domain.BreatheRepository
import kotlinx.coroutines.flow.Flow

class BreatheRepositoryImpl(
    private val breatheDao: BreatheDao
) : BreatheRepository {
    override fun getAllSessions(): Flow<List<BreatheSessionEntity>> = breatheDao.getAllSessions()
    override fun getTotalBreatheDurationFlow(): Flow<Int?> = breatheDao.getTotalBreatheDurationFlow()
    override suspend fun insertSession(session: BreatheSessionEntity) {
        breatheDao.insertSession(session)
    }
    override suspend fun deleteSession(id: Long) {
        breatheDao.deleteSession(id)
    }
    override suspend fun clearAllSessions() {
        breatheDao.clearAllSessions()
    }
}
