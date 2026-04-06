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
            else -> GuideDetail(
                id = guideId,
                title = "Инструкция #$guideId",
                slug = "guide-$guideId",
                description = "Подробная инструкция. Подключитесь к серверу для загрузки полного содержания.",
                difficulty = Difficulty.MEDIUM,
                estimatedTimeMin = 60,
                authorName = "ТехГид",
                componentName = "Общее",
                steps = listOf(
                    GuideStep(
                        id = 100, stepNumber = 1,
                        title = "Подготовка к работе",
                        description = "Подготовьте инструменты и рабочее место. Убедитесь, что у вас есть все необходимые запчасти.",
                        tools = listOf(StepTool("Набор инструментов", "стандартный")),
                        checks = listOf(StepCheck("Все инструменты на месте.", false)),
                    ),
                    GuideStep(
                        id = 101, stepNumber = 2,
                        title = "Выполнение работы",
                        description = "Следуйте рекомендациям производителя. Подключитесь к серверу для получения подробных шагов.",
                        checks = listOf(StepCheck("Работа выполнена.", false)),
                    ),
                    GuideStep(
                        id = 102, stepNumber = 3,
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
