package ru.techgid.data.remote.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import ru.techgid.data.remote.dto.CommentCreateDto
import ru.techgid.data.remote.dto.CommentDto

interface CommentApi {

    @GET("comments/guide/{guideId}")
    suspend fun getGuideComments(
        @Path("guideId") guideId: Int,
        @Query("step_id") stepId: Int? = null,
    ): List<CommentDto>

    @GET("comments/guide/{guideId}/counts")
    suspend fun getCommentCounts(@Path("guideId") guideId: Int): Map<String, Int>

    @POST("comments/")
    suspend fun createComment(@Body body: CommentCreateDto): CommentDto
}
