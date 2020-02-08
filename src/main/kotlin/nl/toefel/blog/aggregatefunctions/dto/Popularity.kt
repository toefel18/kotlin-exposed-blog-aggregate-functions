package nl.toefel.blog.aggregatefunctions.dto

data class Popularity(
    val fromUser: String,
    val content: String,
    val likes: Long,
    val dislikes: Long)