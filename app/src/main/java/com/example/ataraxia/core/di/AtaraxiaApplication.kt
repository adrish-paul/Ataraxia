package com.example.ataraxia.core.di

import android.app.Application

class AtaraxiaApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
