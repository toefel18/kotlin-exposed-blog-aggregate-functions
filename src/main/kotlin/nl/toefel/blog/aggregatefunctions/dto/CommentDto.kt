package nl.toefel.blog.aggregatefunctions.dto

data class CommentDto(
    val fromUser: String,
    val content: String,
    val likes: Long,
    val dislikes: Long)