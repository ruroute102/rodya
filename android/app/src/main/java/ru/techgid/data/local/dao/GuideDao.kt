package ru.techgid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.techgid.data.local.entity.OfflineGuide

@Dao
interface GuideDao {

    @Query("SELECT * FROM offline_guides WHERE configuration_id = :configurationId ORDER BY title")
    fun getOfflineGuides(configurationId: Int): Flow<List<OfflineGuide>>

    @Query("SELECT * FROM offline_guides WHERE id = :guideId")
    suspend fun getOfflineGuide(guideId: Int): OfflineGuide?

    @Query("SELECT EXISTS(SELECT 1 FROM offline_guides WHERE id = :guideId)")
    suspend fun isGuideSaved(guideId: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGuide(guide: OfflineGuide)

    @Query("DELETE FROM offline_guides WHERE id = :guideId")
    suspend fun deleteGuide(guideId: Int)

    @Query("SELECT COUNT(*) FROM offline_guides WHERE configuration_id = :configurationId")
    suspend fun getOfflineCount(configurationId: Int): Int

    @Query("SELECT SUM(length(json_data)) FROM offline_guides WHERE configuration_id = :configurationId")
    suspend fun getOfflineSize(configurationId: Int): Long?
}
