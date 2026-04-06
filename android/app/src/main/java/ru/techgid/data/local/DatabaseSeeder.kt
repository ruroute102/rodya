package ru.techgid.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import ru.techgid.data.local.dao.CarDao
import ru.techgid.data.local.dao.GuideDao
import ru.techgid.data.local.entity.CachedBrand
import ru.techgid.data.local.entity.CachedEngine
import ru.techgid.data.local.entity.CachedGeneration
import ru.techgid.data.local.entity.CachedModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val carDao: CarDao,
    private val guideDao: GuideDao,
    private val dataStore: DataStore<Preferences>,
) {

    companion object {
        private val KEY_DB_SEEDED = booleanPreferencesKey("db_seeded")
    }

    suspend fun seedIfNeeded() {
        val prefs = dataStore.data.first()
        if (prefs[KEY_DB_SEEDED] == true) return

        seedBrands()
        seedModels()
        seedGenerations()
        seedEngines()

        dataStore.edit { it[KEY_DB_SEEDED] = true }
    }

    // ── Марки ──────────────────────────────────────────────────────

    private suspend fun seedBrands() {
        val brands = listOf(
            CachedBrand(id = 1, name = "Audi", slug = "audi", country = "Германия", sortOrder = 1),
            CachedBrand(id = 2, name = "BMW", slug = "bmw", country = "Германия", sortOrder = 2),
            CachedBrand(id = 3, name = "Mercedes-Benz", slug = "mercedes-benz", country = "Германия", sortOrder = 3),
            CachedBrand(id = 4, name = "Toyota", slug = "toyota", country = "Япония", sortOrder = 4),
            CachedBrand(id = 5, name = "Volkswagen", slug = "volkswagen", country = "Германия", sortOrder = 5),
            CachedBrand(id = 6, name = "Kia", slug = "kia", country = "Южная Корея", sortOrder = 6),
            CachedBrand(id = 7, name = "Hyundai", slug = "hyundai", country = "Южная Корея", sortOrder = 7),
            CachedBrand(id = 8, name = "Lada", slug = "lada", country = "Россия", sortOrder = 8),
        )
        carDao.insertBrands(brands)
    }

    // ── Модели ─────────────────────────────────────────────────────

    private suspend fun seedModels() {
        val models = listOf(
            // Audi
            CachedModel(id = 1, brandId = 1, name = "Q3", slug = "q3"),
            CachedModel(id = 2, brandId = 1, name = "Q5", slug = "q5"),
            CachedModel(id = 3, brandId = 1, name = "A4", slug = "a4"),
            CachedModel(id = 4, brandId = 1, name = "A6", slug = "a6"),
            // BMW
            CachedModel(id = 5, brandId = 2, name = "3 Series", slug = "3-series"),
            CachedModel(id = 6, brandId = 2, name = "5 Series", slug = "5-series"),
            CachedModel(id = 7, brandId = 2, name = "X3", slug = "x3"),
            CachedModel(id = 8, brandId = 2, name = "X5", slug = "x5"),
            // Toyota
            CachedModel(id = 9, brandId = 4, name = "Camry", slug = "camry"),
            CachedModel(id = 10, brandId = 4, name = "RAV4", slug = "rav4"),
            CachedModel(id = 11, brandId = 4, name = "Corolla", slug = "corolla"),
            CachedModel(id = 12, brandId = 4, name = "Land Cruiser", slug = "land-cruiser"),
            // Lada
            CachedModel(id = 13, brandId = 8, name = "Vesta", slug = "vesta"),
            CachedModel(id = 14, brandId = 8, name = "Granta", slug = "granta"),
            CachedModel(id = 15, brandId = 8, name = "Niva", slug = "niva"),
            CachedModel(id = 16, brandId = 8, name = "XRAY", slug = "xray"),
        )
        carDao.insertModels(models)
    }

    // ── Поколения ──────────────────────────────────────────────────

    private suspend fun seedGenerations() {
        val generations = listOf(
            CachedGeneration(
                id = 1,
                modelId = 1, // Audi Q3
                name = "8U",
                slug = "8u",
                chassisCode = "8U",
                yearStart = 2011,
                yearEnd = 2018,
            ),
            CachedGeneration(
                id = 2,
                modelId = 1, // Audi Q3
                name = "F3",
                slug = "f3",
                chassisCode = "F3",
                yearStart = 2018,
                yearEnd = null,
            ),
        )
        carDao.insertGenerations(generations)
    }

    // ── Двигатели ──────────────────────────────────────────────────

    private suspend fun seedEngines() {
        val engines = listOf(
            CachedEngine(
                id = 1,
                generationId = 1, // Audi Q3 8U
                code = "CULB",
                name = "2.0 TFSI (211 л.с.)",
                displacementLabel = "2.0",
                fuelType = "бензин",
                powerHp = 211,
            ),
            CachedEngine(
                id = 2,
                generationId = 1, // Audi Q3 8U
                code = "CFFB",
                name = "2.0 TDI (140 л.с.)",
                displacementLabel = "2.0",
                fuelType = "дизель",
                powerHp = 140,
            ),
            CachedEngine(
                id = 3,
                generationId = 1, // Audi Q3 8U
                code = "CZEA",
                name = "1.4 TFSI (150 л.с.)",
                displacementLabel = "1.4",
                fuelType = "бензин",
                powerHp = 150,
            ),
        )
        carDao.insertEngines(engines)
    }
}
