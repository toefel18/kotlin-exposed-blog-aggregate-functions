# Kotlin exposed aggregate functions

Repository that shows how to use aggregate functions.

Run [Main.kt](src/main/kotlin/nl/toefel/blog/alias/Main.kt) for a working server with a REST api at http://localhost:8080/messages

```kotlin

//Table definitions
object MessageTable: Table("message") {
    val id = long("id").autoIncrement()
    val fromUser = long("from_user_id").references(UserTable.id, onDelete = ReferenceOption.CASCADE)
    val toUser = long("to_user_id").references(UserTable.id, onDelete = ReferenceOption.CASCADE)
    val timestamp = datetime("timestamp")
    val content = text("content")

    override val primaryKey = PrimaryKey(id)
}

object UserTable: Table("user") {
    val id = long("id").autoIncrement()
    val name = varchar("owner_type", 64)

    override val primaryKey = PrimaryKey(id)
}

// Query joining the UserTable twice using an alias
val allMessages = transaction {
    val fromUser = UserTable.alias("fromUser")
    val toUser = UserTable.alias("toUser")

    MessageTable
        .join(fromUser, JoinType.INNER, MessageTable.fromUser, fromUser[UserTable.id])
        .join(toUser, JoinType.INNER, MessageTable.toUser, toUser[UserTable.id])
        .selectAll()
        .map { row ->
            MessageDto(
                from = row[fromUser[UserTable.name]],
                to = row[toUser[UserTable.name]],
                timestamp = row[MessageTable.timestamp],
                message = row[MessageTable.content]
            )
        }
}

```