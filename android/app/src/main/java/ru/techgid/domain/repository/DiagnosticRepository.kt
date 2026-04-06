package ru.techgid.domain.repository

import ru.techgid.domain.model.DiagnosticResult
import ru.techgid.domain.model.SymptomCategory

interface DiagnosticRepository {
    suspend fun getSymptomCategories(): List<SymptomCategory>
    suspend fun diagnose(configurationId: Int, symptomIds: List<Int>): List<DiagnosticResult>
}
