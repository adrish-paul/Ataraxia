package com.example.ataraxia.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ataraxia.features.breathe.data.BreatheRepositoryImpl
import com.example.ataraxia.features.breathe.domain.DeleteBreatheSessionUseCase
import com.example.ataraxia.features.breathe.domain.GetBreatheSessionsUseCase
import com.example.ataraxia.features.breathe.domain.SaveBreatheSessionUseCase
import com.example.ataraxia.features.breathe.presentation.BreatheViewModel
import com.example.ataraxia.features.focus.data.FocusRepositoryImpl
import com.example.ataraxia.features.focus.domain.DeleteFocusSessionUseCase
import com.example.ataraxia.features.focus.domain.GetFocusSessionsUseCase
import com.example.ataraxia.features.focus.domain.SaveFocusSessionUseCase
import com.example.ataraxia.features.focus.presentation.FocusViewModel
import com.example.ataraxia.features.journal.data.JournalRepositoryImpl
import com.example.ataraxia.features.journal.domain.DeleteJournalEntryUseCase
import com.example.ataraxia.features.journal.domain.GetJournalEntriesUseCase
import com.example.ataraxia.features.journal.domain.SaveJournalEntryUseCase
import com.example.ataraxia.features.journal.domain.ToggleFavoriteUseCase
import com.example.ataraxia.features.journal.presentation.JournalViewModel
import com.example.ataraxia.viewmodel.MainViewModel

class AtaraxiaViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val journalDao = container.database.journalDao()
        val breatheDao = container.database.breatheDao()
        val focusDao = container.database.focusDao()
        val moodLogDao = container.database.moodLogDao()

        // Breathe
        val breatheRepo = BreatheRepositoryImpl(breatheDao)
        val getBreatheSessionsUseCase = GetBreatheSessionsUseCase(breatheRepo)
        val saveBreatheSessionUseCase = SaveBreatheSessionUseCase(breatheRepo)
        val deleteBreatheSessionUseCase = DeleteBreatheSessionUseCase(breatheRepo)

        // Focus
        val focusRepo = FocusRepositoryImpl(focusDao)
        val getFocusSessionsUseCase = GetFocusSessionsUseCase(focusRepo)
        val saveFocusSessionUseCase = SaveFocusSessionUseCase(focusRepo)
        val deleteFocusSessionUseCase = DeleteFocusSessionUseCase(focusRepo)
        val getFocusIntentionsUseCase = com.example.ataraxia.features.focus.domain.GetFocusIntentionsUseCase(focusRepo)
        val saveFocusIntentionUseCase = com.example.ataraxia.features.focus.domain.SaveFocusIntentionUseCase(focusRepo)

        // Journal
        val journalRepo = JournalRepositoryImpl(journalDao)
        val getJournalEntriesUseCase = GetJournalEntriesUseCase(journalRepo)
        val saveJournalEntryUseCase = SaveJournalEntryUseCase(journalRepo)
        val deleteJournalEntryUseCase = DeleteJournalEntryUseCase(journalRepo)
        val toggleFavoriteUseCase = ToggleFavoriteUseCase(journalRepo)

        return when {
            modelClass.isAssignableFrom(BreatheViewModel::class.java) ->
                BreatheViewModel(getBreatheSessionsUseCase, saveBreatheSessionUseCase, deleteBreatheSessionUseCase) as T

            modelClass.isAssignableFrom(FocusViewModel::class.java) ->
                FocusViewModel(getFocusSessionsUseCase, saveFocusSessionUseCase, deleteFocusSessionUseCase, getFocusIntentionsUseCase, saveFocusIntentionUseCase) as T

            modelClass.isAssignableFrom(JournalViewModel::class.java) ->
                JournalViewModel(getJournalEntriesUseCase, saveJournalEntryUseCase, deleteJournalEntryUseCase, toggleFavoriteUseCase, container.preferences) as T

            modelClass.isAssignableFrom(MainViewModel::class.java) ->
                MainViewModel(container.preferences, journalDao, breatheDao, focusDao, moodLogDao) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
