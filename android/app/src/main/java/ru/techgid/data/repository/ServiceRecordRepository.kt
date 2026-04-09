package ru.techgid.data.repository

import kotlinx.coroutines.flow.Flow
import ru.techgid.data.local.dao.ServiceRecordDao
import ru.techgid.data.local.entity.ServiceRecordEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRecordRepository @Inject constructor(
    private val dao: ServiceRecordDao,
) {
    fun observeAll(): Flow<List<ServiceRecordEntity>> = dao.observeAll()
    fun observeTotalCost(): Flow<Int> = dao.observeTotalCost()
    fun observeMaxMileage(): Flow<Int> = dao.observeMaxMileage()
    fun observeCount(): Flow<Int> = dao.observeCount()

    suspend fun add(record: ServiceRecordEntity): Long = dao.insert(record)
    suspend fun delete(id: Long) = dao.deleteById(id)
}
