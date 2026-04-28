package ru.techgid.data.repository

import com.squareup.moshi.Moshi
import ru.techgid.data.local.dao.GuideDao
import ru.techgid.data.local.entity.OfflineGuide
import ru.techgid.data.mapper.toDomain
import ru.techgid.data.remote.api.CommentApi
import ru.techgid.data.remote.api.GuideApi
import ru.techgid.data.remote.dto.CommentCreateDto
import ru.techgid.data.remote.dto.GuideDetailDto
import ru.techgid.domain.model.Comment
import ru.techgid.domain.model.Difficulty
import ru.techgid.domain.model.GuideDetail
import ru.techgid.domain.model.GuideListItem
import ru.techgid.domain.model.GuidePrecaution
import ru.techgid.domain.model.GuideStep
import ru.techgid.domain.model.StepCheck
import ru.techgid.domain.model.StepConsumable
import ru.techgid.domain.model.StepTool
import ru.techgid.domain.model.StepWarning
import ru.techgid.domain.model.WarningSeverity
import ru.techgid.domain.repository.GuideRepository
import ru.techgid.domain.repository.PaginatedResult
import javax.inject.Inject

class GuideRepositoryImpl @Inject constructor(
    private val guideApi: GuideApi,
    private val commentApi: CommentApi,
    private val guideDao: GuideDao,
    private val moshi: Moshi,
) : GuideRepository {

    override suspend fun getGuides(
        configurationId: Int?,
        componentId: Int?,
        difficulty: String?,
        search: String?,
        page: Int,
        pageSize: Int,
    ): PaginatedResult<GuideListItem> {
        return try {
            val response = guideApi.getGuides(
                configurationId = configurationId,
                componentId = componentId,
                difficulty = difficulty,
                search = search,
                page = page,
                pageSize = pageSize,
            )
            PaginatedResult(
                items = response.items.map { it.toDomain() },
                total = response.total,
                page = response.page,
                pageSize = response.pageSize,
                totalPages = response.pages,
            )
        } catch (_: Exception) {
            getDemoGuides(search, difficulty)
        }
    }

    override suspend fun getGuide(guideId: Int): GuideDetail {
        val offline = guideDao.getOfflineGuide(guideId)
        if (offline != null) {
            val adapter = moshi.adapter(GuideDetailDto::class.java)
            val dto = adapter.fromJson(offline.jsonData)
            if (dto != null) return dto.toDomain()
        }

        return try {
            guideApi.getGuide(guideId).toDomain()
        } catch (_: Exception) {
            getDemoGuideDetail(guideId)
        }
    }

    override suspend fun getComments(guideId: Int, stepId: Int?): List<Comment> {
        return try {
            commentApi.getGuideComments(guideId, stepId).map { it.toDomain() }
        } catch (_: Exception) {
            getDemoComments(guideId, stepId)
        }
    }

    override suspend fun addComment(
        guideId: Int,
        stepId: Int?,
        parentId: Int?,
        text: String,
    ): Comment {
        return try {
            val dto = commentApi.createComment(
                CommentCreateDto(
                    guideId = guideId,
                    stepId = stepId,
                    parentId = parentId,
                    text = text,
                )
            )
            dto.toDomain()
        } catch (_: Exception) {
            Comment(
                id = (System.currentTimeMillis() % 100000).toInt(),
                userId = 0,
                userName = "Вы",
                guideId = guideId,
                stepId = stepId,
                parentId = parentId,
                text = text,
                createdAt = "только что",
            )
        }
    }

    override suspend fun saveGuideOffline(guideId: Int, configurationId: Int) {
        try {
            val dto = guideApi.getGuide(guideId)
            val adapter = moshi.adapter(GuideDetailDto::class.java)
            val json = adapter.toJson(dto)
            guideDao.saveGuide(
                OfflineGuide(
                    id = dto.id,
                    configurationId = configurationId,
                    title = dto.title,
                    slug = dto.slug,
                    difficulty = dto.difficulty,
                    estimatedTimeMin = dto.estimatedTimeMin,
                    componentName = dto.component.name,
                    jsonData = json,
                )
            )
        } catch (_: Exception) {
            // Offline save not possible without API
        }
    }

    override suspend fun isGuideSavedOffline(guideId: Int): Boolean {
        return guideDao.isGuideSaved(guideId)
    }

    override suspend fun removeGuideOffline(guideId: Int) {
        guideDao.deleteGuide(guideId)
    }

    // ── Demo data ─────────────────────────────────────────────────

    private fun getDemoGuides(search: String?, difficulty: String?): PaginatedResult<GuideListItem> {
        var guides = listOf(
            GuideListItem(
                id = 1,
                title = "Замена модуля топливного насоса",
                slug = "fuel-pump-module",
                difficulty = Difficulty.MEDIUM,
                estimatedTimeMin = 90,
                isVerified = true,
                viewsCount = 1240,
                rating = 4.2f,
                ratingCount = 18,
                componentName = "Топливная система",
                authorName = "Алексей",
            ),
            GuideListItem(
                id = 2,
                title = "Замена топливного фильтра",
                slug = "fuel-filter",
                difficulty = Difficulty.EASY,
                estimatedTimeMin = 45,
                isVerified = true,
                viewsCount = 2340,
                rating = 4.6f,
                ratingCount = 45,
                componentName = "Топливная система",
                authorName = "Дмитрий",
            ),
            GuideListItem(
                id = 3,
                title = "Замена масла и фильтра",
                slug = "oil-change",
                difficulty = Difficulty.EASY,
                estimatedTimeMin = 30,
                isVerified = true,
                viewsCount = 5600,
                rating = 4.8f,
                ratingCount = 92,
                componentName = "Двигатель",
                authorName = "Олег",
            ),
            GuideListItem(
                id = 4,
                title = "Замена передних тормозных колодок",
                slug = "front-brake-pads",
                difficulty = Difficulty.MEDIUM,
                estimatedTimeMin = 60,
                isVerified = true,
                viewsCount = 3200,
                rating = 4.5f,
                ratingCount = 56,
                componentName = "Тормозная система",
                authorName = "Иван",
            ),
            GuideListItem(
                id = 5,
                title = "Замена воздушного фильтра",
                slug = "air-filter",
                difficulty = Difficulty.EASY,
                estimatedTimeMin = 15,
                viewsCount = 4100,
                rating = 4.9f,
                ratingCount = 120,
                componentName = "Двигатель",
                authorName = "Сергей",
            ),
            GuideListItem(
                id = 6,
                title = "Замена ремня ГРМ",
                slug = "timing-belt",
                difficulty = Difficulty.HARD,
                estimatedTimeMin = 240,
                isVerified = true,
                viewsCount = 890,
                rating = 4.0f,
                ratingCount = 8,
                componentName = "Двигатель",
                authorName = "Алексей",
            ),
            GuideListItem(
                id = 7,
                title = "Замена катушки зажигания",
                slug = "ignition-coil",
                difficulty = Difficulty.EASY,
                estimatedTimeMin = 20,
                viewsCount = 1800,
                rating = 4.7f,
                ratingCount = 34,
                componentName = "Система зажигания",
                authorName = "Павел",
            ),
            GuideListItem(
                id = 8,
                title = "Замена стойки стабилизатора",
                slug = "stabilizer-link",
                difficulty = Difficulty.MEDIUM,
                estimatedTimeMin = 45,
                viewsCount = 1500,
                rating = 4.3f,
                ratingCount = 22,
                componentName = "Подвеска",
                authorName = "Максим",
            ),
        )

        if (!search.isNullOrBlank()) {
            guides = guides.filter { it.title.contains(search, ignoreCase = true) }
        }
        if (!difficulty.isNullOrBlank()) {
            guides = guides.filter { it.difficulty.name.equals(difficulty, ignoreCase = true) }
        }

        return PaginatedResult(
            items = guides,
            total = guides.size,
            page = 1,
            pageSize = 20,
            totalPages = 1,
        )
    }

    private fun getDemoGuideDetail(guideId: Int): GuideDetail {
        return when (guideId) {
            1 -> GuideDetail(
                id = 1,
                title = "Замена модуля топливного насоса",
                slug = "fuel-pump-module",
                description = "Пошаговая инструкция по замене модуля бензонасоса на Audi Q3 8U с двигателем 2.0 TFSI. Работа средней сложности, займёт около 1.5 часа.",
                difficulty = Difficulty.MEDIUM,
                estimatedTimeMin = 90,
                isVerified = true,
                rating = 4.2f,
                ratingCount = 18,
                authorName = "Алексей",
                componentName = "Модуль топливного насоса",
                steps = listOf(
                    GuideStep(
                        id = 1, stepNumber = 1,
                        title = "Сбросьте давление в топливной системе",
                        description = "Перед началом работы необходимо сбросить остаточное давление в топливной рампе. Извлеките предохранитель бензонасоса (F33) из блока предохранителей и заведите двигатель. Подождите, пока двигатель заглохнет сам — это значит, что давление в системе упало.",
                        warnings = listOf(
                            StepWarning("safety", WarningSeverity.DANGER, "Опасность пожара! Работайте вдали от открытого огня. Бензин крайне летуч."),
                            StepWarning("safety", WarningSeverity.WARNING, "Обязательно отключите клемму «минус» аккумулятора перед работой."),
                        ),
                        tools = listOf(
                            StepTool("Ключ рожковый", "10 мм"),
                            StepTool("Плоскогубцы", "для предохранителя"),
                        ),
                        checks = listOf(
                            StepCheck("Убедитесь, что двигатель полностью заглох самостоятельно.", false),
                            StepCheck("Проверьте, что зажигание выключено.", false),
                        ),
                        commentsCount = 3,
                        waitTimeSeconds = 60,
                    ),
                    GuideStep(
                        id = 2, stepNumber = 2,
                        title = "Снимите заднее сиденье",
                        description = "Потяните подушку заднего сиденья вверх за передний край. Два фиксатора-клипсы отщёлкиваются при вытягивании строго вверх. Не тяните за ткань — только за жёсткий каркас.",
                        warnings = listOf(
                            StepWarning("caution", WarningSeverity.CAUTION, "Фиксаторы хрупкие — тяните ровно вверх, не вбок."),
                        ),
                        tools = listOf(
                            StepTool("Плоская отвёртка", "тонкая, для поддевания", isRequired = false),
                        ),
                        checks = listOf(
                            StepCheck("Оба фиксатора отщелкнулись, подушка свободно снимается.", false),
                        ),
                        commentsCount = 5,
                    ),
                    GuideStep(
                        id = 3, stepNumber = 3,
                        title = "Очистите зону вокруг крышки доступа к насосу",
                        description = "Тщательно протрите область вокруг крышки лючка топливного насоса. Любая грязь, попавшая в бак, может вывести из строя новый насос. Используйте пылесос для удаления крупных частиц, затем протрите безворсовой ветошью.",
                        tools = listOf(
                            StepTool("Мягкая безворсовая ветошь"),
                            StepTool("Пылесос", isRequired = false),
                        ),
                        consumables = listOf(
                            StepConsumable("Ветошь безворсовая", null, "2-3 шт"),
                        ),
                        warnings = listOf(
                            StepWarning("caution", WarningSeverity.CAUTION, "Попадание грязи в бак приведёт к поломке нового насоса!"),
                        ),
                        checks = listOf(
                            StepCheck("Область вокруг крышки абсолютно чистая.", false),
                        ),
                        commentsCount = 2,
                    ),
                    GuideStep(
                        id = 4, stepNumber = 4,
                        title = "Снимите крышку и отсоедините разъёмы",
                        description = "Открутите 4 самореза крышки лючка (Torx T20). Аккуратно отсоедините электрический разъём насоса и топливные шланги. При отсоединении шлангов подложите ветошь — из них вытечет немного бензина.",
                        tools = listOf(
                            StepTool("Отвёртка Torx", "T20"),
                            StepTool("Ветошь для подтёков"),
                        ),
                        warnings = listOf(
                            StepWarning("caution", WarningSeverity.WARNING, "При отсоединении шлангов вытечет бензин. Подготовьте ветошь."),
                        ),
                        checks = listOf(
                            StepCheck("Все разъёмы и шланги отсоединены.", false),
                            StepCheck("Запомнили или сфотографировали порядок подключения.", false),
                        ),
                        commentsCount = 4,
                    ),
                    GuideStep(
                        id = 5, stepNumber = 5,
                        title = "Извлеките старый модуль и установите новый",
                        description = "Поверните прижимное кольцо против часовой стрелки (используйте специальный ключ или деревянную наставку с молотком). Аккуратно извлеките модуль из бака, наклонив его — поплавок датчика уровня должен пройти через отверстие. Установите новый модуль в обратном порядке. Замените уплотнительное кольцо.",
                        tools = listOf(
                            StepTool("Ключ для прижимного кольца", "или деревянная наставка"),
                            StepTool("Молоток", "лёгкий, при использовании наставки"),
                        ),
                        consumables = listOf(
                            StepConsumable("Модуль топливного насоса", "7P0 919 087 E", "1 шт"),
                            StepConsumable("Уплотнительное кольцо", null, "1 шт"),
                        ),
                        warnings = listOf(
                            StepWarning("caution", WarningSeverity.CAUTION, "Не погните поплавок датчика уровня при извлечении!"),
                        ),
                        checks = listOf(
                            StepCheck("Новый модуль плотно сидит в баке.", false),
                            StepCheck("Уплотнительное кольцо заменено.", false),
                            StepCheck("Прижимное кольцо затянуто.", false),
                        ),
                        commentsCount = 6,
                    ),
                    GuideStep(
                        id = 6, stepNumber = 6,
                        title = "Соберите всё обратно и проверьте",
                        description = "Подключите электрический разъём и топливные шланги. Установите крышку лючка (4 самореза T20). Верните предохранитель F33 на место. Подключите клемму аккумулятора. Включите зажигание на 3 секунды (не заводя), выключите, повторите 3 раза — это создаст давление в системе. Заведите двигатель и проверьте на утечки.",
                        tools = listOf(
                            StepTool("Отвёртка Torx", "T20"),
                            StepTool("Ключ рожковый", "10 мм"),
                        ),
                        checks = listOf(
                            StepCheck("Все разъёмы подключены.", false),
                            StepCheck("Предохранитель F33 установлен.", false),
                            StepCheck("Клемма аккумулятора подключена.", false),
                            StepCheck("Двигатель завёлся без проблем.", false),
                            StepCheck("Нет утечек топлива.", true),
                            StepCheck("Уровень топлива отображается корректно.", true),
                        ),
                        commentsCount = 3,
                    ),
                ),
                precautions = listOf(
                    GuidePrecaution("safety", WarningSeverity.DANGER, "Работы с топливной системой — риск возгорания. Работайте в проветриваемом помещении."),
                    GuidePrecaution("safety", WarningSeverity.WARNING, "Отключите аккумулятор перед началом работ."),
                    GuidePrecaution("info", WarningSeverity.INFO, "Рекомендуется заполнить бак не более чем на 1/4 перед работой."),
                ),
            )
            3 -> GuideDetail(
                id = 3,
                title = "Замена масла и масляного фильтра",
                slug = "oil-change",
                description = "Замена моторного масла и масляного фильтра на Audi Q3 8U 2.0 TFSI. Простая процедура, рекомендуется выполнять каждые 10 000 км.",
                difficulty = Difficulty.EASY,
                estimatedTimeMin = 30,
                isVerified = true,
                rating = 4.8f,
                ratingCount = 92,
                authorName = "Олег",
                componentName = "Двигатель",
                steps = listOf(
                    GuideStep(
                        id = 10, stepNumber = 1,
                        title = "Прогрейте двигатель",
                        description = "Заведите двигатель и дайте ему поработать 5-10 минут, чтобы масло стало жидким и лучше стекало.",
                        warnings = listOf(
                            StepWarning("caution", WarningSeverity.CAUTION, "Масло будет горячим! Используйте перчатки."),
                        ),
                        checks = listOf(StepCheck("Двигатель прогрет до рабочей температуры.", false)),
                    ),
                    GuideStep(
                        id = 11, stepNumber = 2,
                        title = "Открутите сливную пробку и слейте масло",
                        description = "Поднимите автомобиль на домкрате или заезжайте на яму. Подставьте ёмкость для отработки (минимум 5 литров). Открутите сливную пробку ключом на 19.",
                        tools = listOf(
                            StepTool("Ключ", "19 мм"),
                            StepTool("Ёмкость для отработки", "5+ литров"),
                            StepTool("Домкрат или яма"),
                        ),
                        consumables = listOf(
                            StepConsumable("Прокладка сливной пробки", "N 013 815 7", "1 шт"),
                        ),
                        checks = listOf(StepCheck("Масло полностью слито (подождите 10-15 минут).", false)),
                        waitTimeSeconds = 900,
                    ),
                    GuideStep(
                        id = 12, stepNumber = 3,
                        title = "Замените масляный фильтр и залейте масло",
                        description = "Открутите корпус масляного фильтра (сверху двигателя, ключ на 32). Замените фильтрующий элемент и уплотнительное кольцо. Закрутите обратно. Закрутите сливную пробку с новой прокладкой. Залейте масло (4.7 литра для 2.0 TFSI).",
                        tools = listOf(
                            StepTool("Ключ для масляного фильтра", "32 мм"),
                            StepTool("Воронка"),
                        ),
                        consumables = listOf(
                            StepConsumable("Масляный фильтр", "06L 115 562", "1 шт"),
                            StepConsumable("Моторное масло 5W-30", "VW 504.00/507.00", "4.7 л"),
                        ),
                        checks = listOf(
                            StepCheck("Уровень масла по щупу — между MIN и MAX.", true),
                            StepCheck("Нет утечек из-под пробки и фильтра.", true),
                            StepCheck("Лампа давления масла не горит после запуска.", true),
                        ),
                    ),
                ),
                precautions = listOf(
                    GuidePrecaution("caution", WarningSeverity.CAUTION, "Горячее масло вызывает ожоги. Работайте в перчатках."),
                    GuidePrecaution("info", WarningSeverity.INFO, "Утилизируйте отработанное масло в специальных пунктах приёма."),
                ),
            )
            2 -> GuideDetail(
                id = 2,
                title = "Замена топливного фильтра",
                slug = "fuel-filter",
                description = "Замена встроенного топливного фильтра на Audi Q3 8U 2.0 TFSI. Фильтр расположен в модуле бензонасоса под задним сиденьем.",
                difficulty = Difficulty.EASY,
                estimatedTimeMin = 45,
                isVerified = true,
                rating = 4.6f,
                ratingCount = 45,
                authorName = "Дмитрий",
                componentName = "Топливная система",
                steps = listOf(
                    GuideStep(
                        id = 20, stepNumber = 1,
                        title = "Сбросьте давление и снимите заднее сиденье",
                        description = "Извлеките предохранитель бензонасоса (F33), заведите двигатель и дождитесь, пока он заглохнет. Отключите минусовую клемму аккумулятора. Снимите подушку заднего сиденья, потянув вверх за передний край.",
                        warnings = listOf(
                            StepWarning("safety", WarningSeverity.DANGER, "Работы с топливом! Обеспечьте вентиляцию, не курите."),
                        ),
                        tools = listOf(
                            StepTool("Ключ рожковый", "10 мм"),
                            StepTool("Плоская отвёртка"),
                        ),
                        checks = listOf(
                            StepCheck("Двигатель заглох — давление сброшено.", false),
                            StepCheck("Клемма аккумулятора отсоединена.", false),
                        ),
                    ),
                    GuideStep(
                        id = 21, stepNumber = 2,
                        title = "Откройте лючок доступа к насосу",
                        description = "Очистите область вокруг крышки лючка. Открутите 4 самореза Torx T20. Отсоедините электрический разъём и топливные шланги. Подложите ветошь — немного бензина вытечет.",
                        tools = listOf(
                            StepTool("Отвёртка Torx", "T20"),
                            StepTool("Ветошь безворсовая"),
                        ),
                        warnings = listOf(
                            StepWarning("caution", WarningSeverity.CAUTION, "При отсоединении шлангов подставьте ветошь."),
                        ),
                        checks = listOf(
                            StepCheck("Крышка снята, разъёмы отсоединены.", false),
                        ),
                    ),
                    GuideStep(
                        id = 22, stepNumber = 3,
                        title = "Извлеките модуль и замените фильтр",
                        description = "Поверните прижимное кольцо против часовой стрелки. Извлеките модуль, наклоняя — аккуратно проведите поплавок через отверстие. Снимите старый фильтр-сеточку с нижней части модуля и установите новый. Проверьте состояние уплотнительного кольца.",
                        tools = listOf(
                            StepTool("Ключ для прижимного кольца"),
                            StepTool("Плоскогубцы"),
                        ),
                        consumables = listOf(
                            StepConsumable("Фильтр топливный (сеточка)", "8K0 919 051 B", "1 шт"),
                            StepConsumable("Уплотнительное кольцо", null, "1 шт"),
                        ),
                        checks = listOf(
                            StepCheck("Новый фильтр установлен.", false),
                            StepCheck("Уплотнительное кольцо на месте.", false),
                        ),
                    ),
                    GuideStep(
                        id = 23, stepNumber = 4,
                        title = "Соберите и проверьте",
                        description = "Установите модуль обратно в бак. Затяните прижимное кольцо. Подключите разъёмы и шланги. Установите крышку (4 самореза T20). Верните предохранитель F33. Подключите аккумулятор. Включите/выключите зажигание 3 раза для создания давления. Запустите двигатель.",
                        tools = listOf(
                            StepTool("Отвёртка Torx", "T20"),
                            StepTool("Ключ рожковый", "10 мм"),
                        ),
                        checks = listOf(
                            StepCheck("Все разъёмы подключены.", false),
                            StepCheck("Двигатель запускается ровно.", true),
                            StepCheck("Нет утечек топлива.", true),
                        ),
                    ),
                ),
                precautions = listOf(
                    GuidePrecaution("safety", WarningSeverity.DANGER, "Топливо легковоспламеняемо. Работайте в проветриваемом помещении."),
                    GuidePrecaution("info", WarningSeverity.INFO, "Заполните бак не более чем на 1/4 перед работой."),
                ),
            )
            4 -> GuideDetail(
                id = 4,
                title = "Замена передних тормозных колодок",
                slug = "front-brake-pads",
                description = "Пошаговая замена передних тормозных колодок на Audi Q3 8U. Работа средней сложности, потребуется домкрат и стандартный набор ключей.",
                difficulty = Difficulty.MEDIUM,
                estimatedTimeMin = 60,
                isVerified = true,
                rating = 4.5f,
                ratingCount = 56,
                authorName = "Иван",
                componentName = "Тормозная система",
                steps = listOf(
                    GuideStep(
                        id = 40, stepNumber = 1,
                        title = "Поднимите автомобиль и снимите колесо",
                        description = "Ослабьте болты колеса (17 мм) до подъёма. Поднимите автомобиль домкратом, установите упор. Снимите колесо.",
                        tools = listOf(
                            StepTool("Баллонный ключ", "17 мм"),
                            StepTool("Домкрат"),
                            StepTool("Упор страховочный"),
                        ),
                        warnings = listOf(
                            StepWarning("safety", WarningSeverity.WARNING, "Обязательно используйте страховочный упор! Не работайте только на домкрате."),
                        ),
                        checks = listOf(
                            StepCheck("Автомобиль надёжно стоит на упоре.", false),
                            StepCheck("Колесо снято.", false),
                        ),
                    ),
                    GuideStep(
                        id = 41, stepNumber = 2,
                        title = "Снимите суппорт",
                        description = "Открутите два направляющих болта суппорта (ключ на 13). Аккуратно снимите суппорт и подвесьте его проволокой — не допускайте нагрузки на тормозной шланг. Извлеките старые колодки.",
                        tools = listOf(
                            StepTool("Ключ", "13 мм"),
                            StepTool("Проволока или крюк", "для подвешивания суппорта"),
                        ),
                        warnings = listOf(
                            StepWarning("caution", WarningSeverity.CAUTION, "Не допускайте, чтобы суппорт висел на тормозном шланге!"),
                        ),
                        checks = listOf(
                            StepCheck("Суппорт надёжно подвешен.", false),
                            StepCheck("Старые колодки извлечены.", false),
                        ),
                    ),
                    GuideStep(
                        id = 42, stepNumber = 3,
                        title = "Вдавите поршень и очистите направляющие",
                        description = "С помощью специального приспособления или старой колодки вдавите поршень суппорта обратно. Перед этим откройте бачок тормозной жидкости — уровень поднимется. Очистите и смажьте направляющие пальцы суппорта.",
                        tools = listOf(
                            StepTool("Приспособление для вдавливания поршня", "или струбцина"),
                            StepTool("Смазка для направляющих", "высокотемпературная"),
                        ),
                        warnings = listOf(
                            StepWarning("caution", WarningSeverity.CAUTION, "Откройте бачок тормозной жидкости перед вдавливанием поршня."),
                            StepWarning("info", WarningSeverity.INFO, "Проверьте уровень жидкости — при необходимости отберите шприцем лишнюю."),
                        ),
                        checks = listOf(
                            StepCheck("Поршень полностью вдавлен.", false),
                            StepCheck("Направляющие смазаны.", false),
                        ),
                    ),
                    GuideStep(
                        id = 43, stepNumber = 4,
                        title = "Установите новые колодки",
                        description = "Установите новые колодки в скобу суппорта. Колодка с датчиком износа — на внутреннюю сторону. Верните суппорт на место и затяните направляющие болты (25 Нм). Установите колесо, опустите автомобиль.",
                        consumables = listOf(
                            StepConsumable("Колодки тормозные передние", "8U0 698 151", "комплект"),
                        ),
                        tools = listOf(
                            StepTool("Ключ", "13 мм"),
                            StepTool("Динамометрический ключ", "25 Нм"),
                            StepTool("Баллонный ключ", "17 мм"),
                        ),
                        checks = listOf(
                            StepCheck("Колодки установлены правильной стороной.", false),
                            StepCheck("Болты затянуты с правильным моментом.", false),
                            StepCheck("Колесо установлено.", false),
                        ),
                    ),
                    GuideStep(
                        id = 44, stepNumber = 5,
                        title = "Прокачайте тормоза и проверьте",
                        description = "Перед началом движения несколько раз нажмите педаль тормоза до упора — поршень должен прижать колодки к диску. Педаль станет жёсткой. Проверьте уровень тормозной жидкости. Закройте бачок. Проедьтесь на малой скорости, проверяя торможение.",
                        checks = listOf(
                            StepCheck("Педаль тормоза жёсткая.", false),
                            StepCheck("Уровень тормозной жидкости в норме.", false),
                            StepCheck("Бачок закрыт.", false),
                            StepCheck("Торможение эффективное, без посторонних звуков.", true),
                            StepCheck("Нет вибрации при торможении.", true),
                        ),
                    ),
                ),
                precautions = listOf(
                    GuidePrecaution("safety", WarningSeverity.WARNING, "Тормоза — критически важная система. При любых сомнениях обратитесь в сервис."),
                    GuidePrecaution("info", WarningSeverity.INFO, "Первые 200 км избегайте резких торможений — колодки должны притереться."),
                ),
            )
            5 -> GuideDetail(
                id = 5,
                title = "Замена воздушного фильтра",
                slug = "air-filter",
                description = "Замена воздушного фильтра двигателя на Audi Q3 8U 2.0 TFSI. Простейшая процедура, не требует инструментов.",
                difficulty = Difficulty.EASY,
                estimatedTimeMin = 15,
                rating = 4.9f,
                ratingCount = 120,
                authorName = "Сергей",
                componentName = "Двигатель",
                steps = listOf(
                    GuideStep(
                        id = 50, stepNumber = 1,
                        title = "Откройте корпус воздушного фильтра",
                        description = "Корпус расположен в левой части моторного отсека. Отщёлкните 4 зажима по периметру крышки. Поднимите крышку вверх.",
                        checks = listOf(StepCheck("Крышка корпуса снята.", false)),
                    ),
                    GuideStep(
                        id = 51, stepNumber = 2,
                        title = "Замените фильтр",
                        description = "Извлеките старый фильтрующий элемент. Протрите внутреннюю поверхность корпуса влажной ветошью. Установите новый фильтр — убедитесь, что он плотно сидит в пазах.",
                        consumables = listOf(
                            StepConsumable("Воздушный фильтр", "8U0 129 620 D", "1 шт"),
                        ),
                        tools = listOf(StepTool("Ветошь влажная")),
                        checks = listOf(
                            StepCheck("Корпус очищен от пыли и листьев.", false),
                            StepCheck("Новый фильтр установлен ровно.", false),
                        ),
                    ),
                    GuideStep(
                        id = 52, stepNumber = 3,
                        title = "Закройте корпус",
                        description = "Опустите крышку и защёлкните все 4 зажима. Убедитесь, что крышка плотно прилегает по всему периметру.",
                        checks = listOf(
                            StepCheck("Все 4 зажима защёлкнуты.", false),
                            StepCheck("Крышка не болтается.", true),
                        ),
                    ),
                ),
            )
            6 -> GuideDetail(
                id = 6,
                title = "Замена ремня ГРМ",
                slug = "timing-belt",
                description = "Замена ремня газораспределительного механизма на Audi Q3 8U 2.0 TFSI. Сложная работа, требует специального инструмента и опыта.",
                difficulty = Difficulty.HARD,
                estimatedTimeMin = 240,
                isVerified = true,
                rating = 4.0f,
                ratingCount = 8,
                authorName = "Алексей",
                componentName = "Двигатель",
                steps = listOf(
                    GuideStep(
                        id = 60, stepNumber = 1,
                        title = "Снимите переднюю часть автомобиля",
                        description = "Снимите передний бампер, решётку радиатора и замок капота для доступа к ремню. Слейте охлаждающую жидкость. Снимите ремень генератора/кондиционера.",
                        tools = listOf(
                            StepTool("Набор Torx", "T25, T30, T45"),
                            StepTool("Ключи", "10, 13, 16 мм"),
                            StepTool("Ёмкость для ОЖ", "6+ литров"),
                        ),
                        warnings = listOf(
                            StepWarning("safety", WarningSeverity.DANGER, "Сложная работа! При неправильной установке меток — поршни встретятся с клапанами."),
                        ),
                        checks = listOf(StepCheck("Доступ к ремню ГРМ открыт.", false)),
                    ),
                    GuideStep(
                        id = 61, stepNumber = 2,
                        title = "Установите метки ГРМ",
                        description = "Проверните коленвал до совмещения меток. Зафиксируйте коленвал и распредвалы специальными фиксаторами. Ещё раз проверьте совмещение всех меток.",
                        tools = listOf(
                            StepTool("Фиксатор коленвала", "T10340"),
                            StepTool("Фиксаторы распредвалов", "T10339"),
                            StepTool("Торцевая головка", "для коленвала"),
                        ),
                        warnings = listOf(
                            StepWarning("safety", WarningSeverity.DANGER, "Метки ОБЯЗАНЫ совпадать! Перепроверьте перед снятием ремня."),
                        ),
                        checks = listOf(
                            StepCheck("Метки коленвала совмещены.", false),
                            StepCheck("Метки распредвалов совмещены.", false),
                            StepCheck("Фиксаторы установлены.", false),
                        ),
                    ),
                    GuideStep(
                        id = 62, stepNumber = 3,
                        title = "Снимите старый ремень и ролики",
                        description = "Ослабьте натяжитель. Снимите ремень ГРМ. Замените натяжной и обводной ролики. Осмотрите водяной насос — при необходимости замените.",
                        consumables = listOf(
                            StepConsumable("Комплект ГРМ (ремень + ролики)", "INA 530 0550 10", "1 комплект"),
                            StepConsumable("Водяной насос", "06L 121 012 A", "при необходимости"),
                        ),
                        checks = listOf(
                            StepCheck("Старый ремень и ролики сняты.", false),
                            StepCheck("Новые ролики установлены.", false),
                        ),
                    ),
                    GuideStep(
                        id = 63, stepNumber = 4,
                        title = "Установите новый ремень",
                        description = "Наденьте новый ремень, соблюдая направление стрелок. Отпустите натяжитель — он автоматически натянет ремень. Извлеките фиксаторы. Проверните коленвал на 2 полных оборота вручную. Снова проверьте метки.",
                        warnings = listOf(
                            StepWarning("safety", WarningSeverity.DANGER, "После установки проверните вал ВРУЧНУЮ — никогда стартером!"),
                        ),
                        checks = listOf(
                            StepCheck("Ремень установлен по стрелкам.", false),
                            StepCheck("Натяжитель работает.", false),
                            StepCheck("Коленвал прокручен на 2 оборота.", false),
                            StepCheck("Метки совпадают после прокрутки.", false),
                        ),
                    ),
                    GuideStep(
                        id = 64, stepNumber = 5,
                        title = "Соберите всё обратно",
                        description = "Установите защитные кожухи, ремень генератора, залейте охлаждающую жидкость. Соберите переднюю часть автомобиля. Запустите двигатель и прислушайтесь.",
                        checks = listOf(
                            StepCheck("Охлаждающая жидкость залита.", false),
                            StepCheck("Двигатель работает ровно.", true),
                            StepCheck("Нет посторонних звуков.", true),
                            StepCheck("Нет утечек ОЖ.", true),
                        ),
                    ),
                ),
                precautions = listOf(
                    GuidePrecaution("safety", WarningSeverity.DANGER, "Ошибка при установке ГРМ приведёт к дорогостоящему ремонту двигателя!"),
                    GuidePrecaution("info", WarningSeverity.INFO, "Если нет опыта — доверьте эту работу профессионалам."),
                ),
            )
            7 -> GuideDetail(
                id = 7,
                title = "Замена катушки зажигания",
                slug = "ignition-coil",
                description = "Замена индивидуальной катушки зажигания на Audi Q3 8U 2.0 TFSI. Простая процедура — 15-20 минут.",
                difficulty = Difficulty.EASY,
                estimatedTimeMin = 20,
                rating = 4.7f,
                ratingCount = 34,
                authorName = "Павел",
                componentName = "Система зажигания",
                steps = listOf(
                    GuideStep(
                        id = 70, stepNumber = 1,
                        title = "Снимите пластиковую крышку двигателя",
                        description = "Потяните крышку двигателя вверх — она держится на 4 резиновых втулках. Под ней — 4 катушки зажигания.",
                        checks = listOf(StepCheck("Крышка двигателя снята.", false)),
                    ),
                    GuideStep(
                        id = 71, stepNumber = 2,
                        title = "Замените неисправную катушку",
                        description = "Отсоедините электрический разъём от неисправной катушки. Открутите крепёжный болт (Torx T30). Извлеките катушку движением вверх. Установите новую в обратном порядке.",
                        tools = listOf(
                            StepTool("Отвёртка Torx", "T30"),
                        ),
                        consumables = listOf(
                            StepConsumable("Катушка зажигания", "06H 905 110 G", "1 шт"),
                        ),
                        checks = listOf(
                            StepCheck("Новая катушка установлена.", false),
                            StepCheck("Разъём подключён.", false),
                            StepCheck("Болт затянут.", false),
                        ),
                    ),
                    GuideStep(
                        id = 72, stepNumber = 3,
                        title = "Проверьте работу",
                        description = "Установите крышку двигателя. Запустите двигатель. Пропуски зажигания должны исчезнуть, Check Engine погаснет через несколько циклов.",
                        checks = listOf(
                            StepCheck("Двигатель работает ровно.", true),
                            StepCheck("Нет пропусков зажигания.", true),
                        ),
                    ),
                ),
            )
            8 -> GuideDetail(
                id = 8,
                title = "Замена стойки стабилизатора",
                slug = "stabilizer-link",
                description = "Замена стойки (тяги) переднего стабилизатора поперечной устойчивости на Audi Q3 8U. Типичная причина стуков в подвеске.",
                difficulty = Difficulty.MEDIUM,
                estimatedTimeMin = 45,
                rating = 4.3f,
                ratingCount = 22,
                authorName = "Максим",
                componentName = "Подвеска",
                steps = listOf(
                    GuideStep(
                        id = 80, stepNumber = 1,
                        title = "Поднимите автомобиль и снимите колесо",
                        description = "Ослабьте болты колеса. Поднимите автомобиль домкратом, установите страховочный упор. Снимите колесо.",
                        tools = listOf(
                            StepTool("Баллонный ключ", "17 мм"),
                            StepTool("Домкрат"),
                            StepTool("Упор страховочный"),
                        ),
                        checks = listOf(StepCheck("Автомобиль на упоре, колесо снято.", false)),
                    ),
                    GuideStep(
                        id = 81, stepNumber = 2,
                        title = "Открутите старую стойку",
                        description = "Стойка крепится двумя гайками — сверху к стойке амортизатора, снизу к стабилизатору. Используйте ключ на 16 и внутренний Torx T50 для удержания пальца от проворачивания.",
                        tools = listOf(
                            StepTool("Ключ рожковый", "16 мм"),
                            StepTool("Torx", "T50"),
                            StepTool("Проникающая смазка", "WD-40"),
                        ),
                        warnings = listOf(
                            StepWarning("caution", WarningSeverity.CAUTION, "Нанесите проникающую смазку за 10-15 минут до откручивания."),
                        ),
                        checks = listOf(StepCheck("Старая стойка снята.", false)),
                        waitTimeSeconds = 600,
                    ),
                    GuideStep(
                        id = 82, stepNumber = 3,
                        title = "Установите новую стойку",
                        description = "Установите новую стойку стабилизатора. Затяните гайки моментом 50 Нм. Установите колесо, опустите автомобиль. Протяните колёсные болты (120 Нм).",
                        consumables = listOf(
                            StepConsumable("Стойка стабилизатора", "5Q0 411 315 A", "1 шт"),
                        ),
                        tools = listOf(
                            StepTool("Динамометрический ключ", "50 Нм / 120 Нм"),
                            StepTool("Баллонный ключ", "17 мм"),
                        ),
                        checks = listOf(
                            StepCheck("Гайки затянуты с правильным моментом.", false),
                            StepCheck("Колесо установлено и протянуто.", false),
                            StepCheck("Стук при проезде неровностей исчез.", true),
                        ),
                    ),
                ),
            )
            else -> GuideDetail(
                id = guideId,
                title = "Инструкция #$guideId",
                slug = "guide-$guideId",
                description = "Подробная инструкция по ремонту.",
                difficulty = Difficulty.MEDIUM,
                estimatedTimeMin = 60,
                authorName = "ТехГид",
                componentName = "Общее",
                steps = listOf(
                    GuideStep(
                        id = 100 + guideId * 10, stepNumber = 1,
                        title = "Подготовка к работе",
                        description = "Подготовьте инструменты и рабочее место. Убедитесь, что у вас есть все необходимые запчасти.",
                        tools = listOf(StepTool("Набор инструментов", "стандартный")),
                        checks = listOf(StepCheck("Все инструменты на месте.", false)),
                    ),
                    GuideStep(
                        id = 101 + guideId * 10, stepNumber = 2,
                        title = "Выполнение работы",
                        description = "Следуйте рекомендациям производителя.",
                        checks = listOf(StepCheck("Работа выполнена.", false)),
                    ),
                    GuideStep(
                        id = 102 + guideId * 10, stepNumber = 3,
                        title = "Проверка результата",
                        description = "Проверьте результат работы. Убедитесь, что всё функционирует корректно.",
                        checks = listOf(
                            StepCheck("Всё работает исправно.", true),
                            StepCheck("Нет утечек и посторонних шумов.", true),
                        ),
                    ),
                ),
            )
        }
    }

    private fun getDemoComments(guideId: Int, stepId: Int?): List<Comment> = listOf(
        Comment(
            id = 1, userId = 10, userName = "Алексей",
            userCarDisplay = "Владелец Audi Q3 2013",
            guideId = guideId, stepId = stepId,
            text = "Отличная инструкция! Сделал всё за полтора часа. На моём 2013 году лючок чуть правее — имейте в виду.",
            createdAt = "24 апр",
            replies = listOf(
                Comment(
                    id = 3, userId = 11, userName = "Дмитрий",
                    userCarDisplay = "Владелец Audi Q3 2015",
                    guideId = guideId, stepId = stepId, parentId = 1,
                    text = "У меня тоже 2013, подтверждаю — чуть правее. А так всё совпадает.",
                    createdAt = "25 апр",
                ),
            ),
        ),
        Comment(
            id = 2, userId = 12, userName = "Эрик",
            userCarDisplay = "Владелец Audi Q3 2011",
            guideId = guideId, stepId = stepId,
            text = "Спасибо за совет про пылесос — реально много грязи было. После чистки всё прошло гладко.",
            createdAt = "14 апр",
        ),
        Comment(
            id = 4, userId = 13, userName = "Ольга",
            userCarDisplay = "Владелец Audi Q3 2016",
            guideId = guideId, stepId = stepId,
            text = "Первый раз делала сама — получилось! Фото в инструкции очень помогли.",
            createdAt = "10 апр",
        ),
    )
}
