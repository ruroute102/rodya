package ru.techgid.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.techgid.data.local.entity.ServiceRecordEntity

@Dao
interface ServiceRecordDao {

    @Query("SELECT * FROM service_records ORDER BY date_iso DESC, id DESC")
    fun observeAll(): Flow<List<ServiceRecordEntity>>

    @Query("SELECT COALESCE(SUM(cost_rub), 0) FROM service_records")
    fun observeTotalCost(): Flow<Int>

    @Query("SELECT COALESCE(MAX(mileage_km), 0) FROM service_records")
    fun observeMaxMileage(): Flow<Int>

    @Query("SELECT COUNT(*) FROM service_records")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: ServiceRecordEntity): Long

    @Delete
    suspend fun delete(record: ServiceRecordEntity)

    @Query("DELETE FROM service_records WHERE id = :id")
    suspend fun deleteById(id: Long)
}
