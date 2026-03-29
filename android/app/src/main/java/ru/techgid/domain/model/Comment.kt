package ru.techgid.domain.model

/**
 * Domain-модели комментариев.
 */
data class Comment(
    val id: Int,
    val userId: Int,
    val userName: String,
    val userAvatar: String? = null,
    val userCarDisplay: String? = null, // "Владелец Audi Q3 2011"
    val guideId: Int,
    val stepId: Int? = null,
    val parentId: Int? = null,
    val text: String,
    val createdAt: String,
    val replies: List<Comment> = emptyList(),
)
