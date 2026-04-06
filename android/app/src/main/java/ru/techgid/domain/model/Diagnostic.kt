package ru.techgid.domain.model

data class SymptomCategory(
    val id: Int,
    val name: String,
    val slug: String,
    val symptoms: List<Symptom> = emptyList(),
)

data class Symptom(
    val id: Int,
    val categoryId: Int,
    val name: String,
    val description: String? = null,
)

data class DiagnosticResult(
    val cause: String,
    val probability: Float,
    val componentName: String? = null,
    val guideId: Int? = null,
    val guideTitle: String? = null,
    val checks: List<String> = emptyList(),
)
