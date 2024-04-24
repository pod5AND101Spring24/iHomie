package com.example.ihomie

import android.app.Application
import android.content.Context
import androidx.room.Room

class SavedHomesApplication : Application() {
    val db by lazy { AppDatabase.getInstance(this) }
}
