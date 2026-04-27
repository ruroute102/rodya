package ru.techgid.data.repository

import kotlinx.coroutines.flow.Flow
import ru.techgid.data.local.dao.ReminderDao
import ru.techgid.data.local.entity.ReminderEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepository @Inject constructor(
    private val dao: ReminderDao,
) {
    fun observeAll(): Flow<List<ReminderEntity>> = dao.observeAll()

    suspend fun add(
        title: String,
        dueMileage: Int?,
        dueDateIso: String?,
    ): Long = dao.insert(
        ReminderEntity(
            title = title,
            dueMileage = dueMileage,
            dueDateIso = dueDateIso,
        )
    )

    suspend fun toggleDone(reminder: ReminderEntity) {
        dao.update(reminder.copy(isDone = !reminder.isDone))
    }

    suspend fun delete(id: Long) = dao.deleteById(id)

    suspend fun restore(reminder: ReminderEntity): Long = dao.insert(reminder)
}
