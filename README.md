# Kotlin exposed aggregate functions

Repository that shows how to use aggregate functions.

Run [Main.kt](src/main/kotlin/nl/toefel/blog/aggregatefunctions/Main.kt) for a working server with a REST api at 
- http://localhost:8080/comments 
- http://localhost:8080/user-statistics 
- http://localhost:8080/user-popularity 

```kotlin
object UserTable: Table("user") {
    val id = long("id").autoIncrement()
    val name = varchar("owner_type", 64)

    override val primaryKey = PrimaryKey(id)
}

object CommentTable: Table("comment") {
    val id = long("id").autoIncrement()
    val fromUser = long("from_user_id").references(UserTable.id, onDelete = ReferenceOption.CASCADE)
    val content = text("content")

    val likes = long("likes").default(0L)
    val dislikes = long("dislikes").default(0L)

    override val primaryKey = PrimaryKey(id)
}


fun listComments(ctx: Context) {
    val allMessages = transaction {
        CommentTable
            .innerJoin(UserTable)
            .selectAll()
            .map { row ->
                CommentDto(
                    fromUser = row[UserTable.name],
                    content = row[CommentTable.content],
                    likes = row[CommentTable.likes],
                    dislikes = row[CommentTable.dislikes]
                )
            }
    }

    ctx.json(allMessages)
}

fun userStatistics(ctx: Context) {
    val userStatistics = transaction {
        CommentTable
            .innerJoin(UserTable)
            .slice(UserTable.name, CommentTable.likes.sum(), CommentTable.dislikes.sum())
            .selectAll()
            .groupBy(UserTable.name)
            .map { row ->
                UserStatistic(
                    fromUser = row[UserTable.name],
                    totalLikes = row[CommentTable.likes.sum()] ?: -1,
                    totalDislikes = row[CommentTable.dislikes.sum()] ?: -1
                )
            }
    }

    ctx.json(userStatistics)
}


fun userPopularity(ctx: Context) {
    val userPopularityScores = transaction {

        val commentPopularityScore = CommentTable.likes - CommentTable.dislikes

        CommentTable
            .innerJoin(UserTable)
            .slice(UserTable.name, commentPopularityScore.sum())
            .selectAll()
            .groupBy(UserTable.name)
            .map { row ->
                println("""
                    user = ${row[UserTable.name]},
                    popularityScore = ${row[commentPopularityScore.sum()] ?: 0L}
                """.trimIndent())

                UserPopularityScore(
                    fromUser = row[UserTable.name],
                    popularityScore = row[commentPopularityScore.sum()] ?: 0L
                )
            }
    }

    ctx.json(userPopularityScores)
}
```