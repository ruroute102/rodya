package ru.techgid.data.repository

import ru.techgid.data.remote.api.DiagnosticApi
import ru.techgid.data.remote.dto.DiagnosticRequestDto
import ru.techgid.domain.model.DiagnosticResult
import ru.techgid.domain.model.Symptom
import ru.techgid.domain.model.SymptomCategory
import ru.techgid.domain.repository.DiagnosticRepository
import javax.inject.Inject

class DiagnosticRepositoryImpl @Inject constructor(
    private val diagnosticApi: DiagnosticApi,
) : DiagnosticRepository {

    override suspend fun getSymptomCategories(): List<SymptomCategory> {
        return try {
            val categories = diagnosticApi.getSymptomCategories()
            categories.map { cat ->
                val symptoms = diagnosticApi.getSymptoms(cat.id)
                SymptomCategory(
                    id = cat.id,
                    name = cat.name,
                    slug = cat.slug,
                    symptoms = symptoms.map { s ->
                        Symptom(
                            id = s.id,
                            categoryId = s.categoryId,
                            name = s.name,
                            description = s.description,
                        )
                    },
                )
            }
        } catch (_: Exception) {
            getDemoSymptomCategories()
        }
    }

    override suspend fun diagnose(configurationId: Int, symptomIds: List<Int>): List<DiagnosticResult> {
        return try {
            val results = diagnosticApi.diagnose(
                DiagnosticRequestDto(configurationId, symptomIds)
            )
            results.map { r ->
                DiagnosticResult(
                    cause = r.probableCause,
                    probability = r.probability,
                    componentName = r.componentName,
                    guideId = r.guideId,
                    guideTitle = r.guideTitle,
                    checks = r.checks.map { it.description },
                )
            }
        } catch (_: Exception) {
            getDemoDiagnosticResults(symptomIds)
        }
    }

    private fun getDemoSymptomCategories(): List<SymptomCategory> = listOf(
        SymptomCategory(1, "Двигатель", "engine", listOf(
            Symptom(1, 1, "Не заводится"),
            Symptom(2, 1, "Плавают обороты"),
            Symptom(3, 1, "Троит"),
            Symptom(4, 1, "Посторонний стук"),
            Symptom(5, 1, "Повышенный расход топлива"),
        )),
        SymptomCategory(2, "Топливная система", "fuel", listOf(
            Symptom(6, 2, "Запах бензина в салоне"),
            Symptom(7, 2, "Потеря мощности"),
            Symptom(8, 2, "Долго заводится"),
        )),
        SymptomCategory(3, "Электрика", "electrical", listOf(
            Symptom(9, 3, "Горит Check Engine"),
            Symptom(10, 3, "Не работают приборы"),
            Symptom(11, 3, "Проблемы с зарядкой"),
        )),
        SymptomCategory(4, "Тормозная система", "brakes", listOf(
            Symptom(12, 4, "Скрип при торможении"),
            Symptom(13, 4, "Увеличенный тормозной путь"),
            Symptom(14, 4, "Вибрация при торможении"),
        )),
        SymptomCategory(5, "Подвеска", "suspension", listOf(
            Symptom(15, 5, "Стук на неровностях"),
            Symptom(16, 5, "Увод в сторону"),
            Symptom(17, 5, "Неравномерный износ шин"),
        )),
    )

    private fun getDemoDiagnosticResults(symptomIds: List<Int>): List<DiagnosticResult> {
        val results = mutableListOf<DiagnosticResult>()

        if (symptomIds.any { it in listOf(1, 6, 7, 8) }) {
            results.add(DiagnosticResult(
                cause = "Неисправность бензонасоса",
                probability = 0.75f,
                componentName = "Модуль топливного насоса",
                guideId = 1,
                guideTitle = "Замена модуля топливного насоса",
                checks = listOf(
                    "Проверить давление в рампе. Норма: 4–5 бар",
                    "Послушать работу насоса при включении зажигания",
                    "Проверить напряжение на разъёме насоса (12В)",
                ),
            ))
            results.add(DiagnosticResult(
                cause = "Засорён топливный фильтр",
                probability = 0.45f,
                componentName = "Топливный фильтр",
                guideId = 2,
                guideTitle = "Замена топливного фильтра",
                checks = listOf(
                    "Проверить перепад давления до и после фильтра",
                ),
            ))
        }

        if (symptomIds.any { it in listOf(2, 3, 5, 9) }) {
            results.add(DiagnosticResult(
                cause = "Неисправность катушки зажигания",
                probability = 0.60f,
                componentName = "Катушка зажигания",
                guideId = 7,
                guideTitle = "Замена катушки зажигания",
                checks = listOf(
                    "Проверить сопротивление первичной обмотки (0.4–0.6 Ом)",
                    "Проверить сопротивление вторичной обмотки (6–12 кОм)",
                    "Осмотреть на наличие трещин и следов пробоя",
                ),
            ))
        }

        if (symptomIds.any { it in listOf(4) }) {
            results.add(DiagnosticResult(
                cause = "Износ гидрокомпенсаторов",
                probability = 0.55f,
                componentName = "Гидрокомпенсаторы",
                checks = listOf(
                    "Стук усиливается при холодном запуске",
                    "Проверить давление масла",
                    "Проверить уровень и состояние масла",
                ),
            ))
        }

        if (symptomIds.any { it in listOf(9, 10, 11) }) {
            results.add(DiagnosticResult(
                cause = "Неисправность датчика положения коленвала",
                probability = 0.20f,
                componentName = "ДПКВ",
                checks = listOf(
                    "Проверить сопротивление датчика (700–900 Ом)",
                    "Проверить осциллограмму сигнала",
                ),
            ))
        }

        if (results.isEmpty()) {
            results.add(DiagnosticResult(
                cause = "Требуется углублённая диагностика",
                probability = 0.30f,
                checks = listOf(
                    "Подключить диагностический сканер",
                    "Считать коды ошибок",
                    "Проверить параметры в реальном времени",
                ),
            ))
        }

        return results.sortedByDescending { it.probability }
    }
}
