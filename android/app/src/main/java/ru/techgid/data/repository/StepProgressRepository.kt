package ru.techgid.data.repository

import kotlinx.coroutines.flow.Flow
import ru.techgid.data.local.dao.StepProgressDao
import ru.techgid.data.local.entity.StepProgressEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepProgressRepository @Inject constructor(
    private val dao: StepProgressDao,
) {
    fun observeForGuide(guideId: Int): Flow<List<StepProgressEntity>> =
        dao.observeForGuide(guideId)

    fun observeDoneCount(guideId: Int): Flow<Int> = dao.observeDoneCount(guideId)

    suspend fun setStepDone(guideId: Int, stepId: Int, isDone: Boolean) {
        dao.upsert(
            StepProgressEntity(
                guideId = guideId,
                stepId = stepId,
                isDone = isDone,
            )
        )
    }

    suspend fun resetGuide(guideId: Int) = dao.clearForGuide(guideId)
}
