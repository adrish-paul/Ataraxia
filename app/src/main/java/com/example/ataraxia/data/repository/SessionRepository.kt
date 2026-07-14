package com.example.ataraxia.data.repository

import com.example.ataraxia.data.local.dao.BreatheDao
import com.example.ataraxia.data.local.dao.FocusDao
import com.example.ataraxia.data.local.entity.BreatheSessionEntity
import com.example.ataraxia.data.local.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow

class SessionRepository(
    private val breatheDao: BreatheDao,
    private val focusDao: FocusDao
) {
    val allBreatheSessions: Flow<List<BreatheSessionEntity>> = breatheDao.getAllSessions()
    val totalBreatheDurationFlow: Flow<Int?> = breatheDao.getTotalBreatheDurationFlow()

    val allFocusSessions: Flow<List<FocusSessionEntity>> = focusDao.getAllSessions()
    val totalFocusDurationFlow: Flow<Int?> = focusDao.getTotalFocusDurationFlow()

    suspend fun insertBreatheSession(session: BreatheSessionEntity) {
        breatheDao.insertSession(session)
    }

    suspend fun deleteBreatheSession(id: Long) {
        breatheDao.deleteSession(id)
    }

    suspend fun insertFocusSession(session: FocusSessionEntity) {
        focusDao.insertSession(session)
    }

    suspend fun deleteFocusSession(id: Long) {
        focusDao.deleteSession(id)
    }

    suspend fun clearAllSessions() {
        breatheDao.clearAllSessions()
        focusDao.clearAllSessions()
    }
}
