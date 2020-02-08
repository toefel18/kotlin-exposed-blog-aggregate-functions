package nl.toefel.blog.aggregatefunctions.dto

data class UserPopularityScore(
    val fromUser: String,
    val popularityScore: Long)