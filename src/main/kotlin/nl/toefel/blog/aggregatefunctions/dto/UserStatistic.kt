package nl.toefel.blog.aggregatefunctions.dto

data class UserStatistic(
    val fromUser: String,
    val totalLikes: Long,
    val totalDislikes: Long)