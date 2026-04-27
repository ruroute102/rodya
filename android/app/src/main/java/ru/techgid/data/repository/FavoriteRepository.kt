package ru.techgid.data.repository

import kotlinx.coroutines.flow.Flow
import ru.techgid.data.local.dao.FavoriteDao
import ru.techgid.data.local.entity.FavoriteEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val dao: FavoriteDao,
) {
    fun observeAll(): Flow<List<FavoriteEntity>> = dao.observeAll()
    fun observeCount(): Flow<Int> = dao.observeCount()
    fun observeIsFavorite(guideId: Int): Flow<Boolean> = dao.observeIsFavorite(guideId)

    suspend fun isFavorite(guideId: Int): Boolean = dao.isFavorite(guideId)

    suspend fun toggle(
        guideId: Int,
        title: String,
        componentName: String,
        difficulty: String,
        estimatedTimeMin: Int?,
    ) {
        if (dao.isFavorite(guideId)) {
            dao.remove(guideId)
        } else {
            dao.add(
                FavoriteEntity(
                    guideId = guideId,
                    title = title,
                    componentName = componentName,
                    difficulty = difficulty,
                    estimatedTimeMin = estimatedTimeMin,
                )
            )
        }
    }

    suspend fun remove(guideId: Int) = dao.remove(guideId)

    suspend fun restore(favorite: FavoriteEntity) = dao.add(favorite)
}
