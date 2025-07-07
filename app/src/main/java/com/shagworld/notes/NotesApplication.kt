package com.shagworld.notes

import android.app.Application
import com.shagworld.notes.uitls.Pref

class NotesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Pref.init(this)
    }
}