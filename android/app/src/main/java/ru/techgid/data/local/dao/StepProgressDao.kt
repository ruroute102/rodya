package ru.techgid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.techgid.data.local.entity.StepProgressEntity

@Dao
interface StepProgressDao {

    @Query("SELECT * FROM step_progress WHERE guide_id = :guideId")
    fun observeForGuide(guideId: Int): Flow<List<StepProgressEntity>>

    @Query("SELECT COUNT(*) FROM step_progress WHERE guide_id = :guideId AND is_done = 1")
    fun observeDoneCount(guideId: Int): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: StepProgressEntity)

    @Query("DELETE FROM step_progress WHERE guide_id = :guideId")
    suspend fun clearForGuide(guideId: Int)
}
