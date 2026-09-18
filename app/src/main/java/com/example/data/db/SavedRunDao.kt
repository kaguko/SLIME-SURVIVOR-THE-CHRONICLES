package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedRunDao {
    @Query("SELECT * FROM saved_run WHERE id = 1 LIMIT 1")
    fun getSavedRunFlow(): Flow<SavedRunEntity?>

    @Query("SELECT * FROM saved_run WHERE id = 1 LIMIT 1")
    suspend fun getSavedRun(): SavedRunEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRun(run: SavedRunEntity)

    @Query("DELETE FROM saved_run WHERE id = 1")
    suspend fun clearSavedRun()
}
