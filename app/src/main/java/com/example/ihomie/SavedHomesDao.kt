package com.example.ihomie
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SavedHomesDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(savedHomes: SavedHomes)

    @Query("DELETE FROM saved_homes WHERE zpid = :zpid")
    suspend fun delete(zpid: String)

    @Query("SELECT * FROM saved_homes")
    suspend fun getAllSavedHomes(): List<SavedHomes>
}