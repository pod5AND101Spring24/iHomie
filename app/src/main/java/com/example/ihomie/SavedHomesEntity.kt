package com.example.ihomie
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_homes")
data class SavedHomes(
    @PrimaryKey val zpid: String
)