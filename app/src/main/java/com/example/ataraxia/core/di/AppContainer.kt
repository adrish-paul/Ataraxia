package com.example.ataraxia.core.di

import android.content.Context
import com.example.ataraxia.data.local.AtaraxiaDatabase
import com.example.ataraxia.data.preferences.SanctuaryPreferences

interface AppContainer {
    val database: AtaraxiaDatabase
    val preferences: SanctuaryPreferences
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val database: AtaraxiaDatabase by lazy {
        AtaraxiaDatabase.getDatabase(context)
    }

    override val preferences: SanctuaryPreferences by lazy {
        SanctuaryPreferences(context)
    }
}
