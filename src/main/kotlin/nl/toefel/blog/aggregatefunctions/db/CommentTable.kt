package nl.toefel.blog.aggregatefunctions.db

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object CommentTable: Table("comment") {
    val id = long("id").autoIncrement()
    val fromUser = long("from_user_id").references(UserTable.id, onDelete = ReferenceOption.CASCADE)
    val content = text("content")

    val likes = long("likes").default(0L)
    val dislikes = long("dislikes").default(0L)

    override val primaryKey = PrimaryKey(id)
}