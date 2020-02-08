package nl.toefel.blog.aggregatefunctions.db

import org.jetbrains.exposed.sql.Table

object UserTable: Table("user") {
    val id = long("id").autoIncrement()
    val name = varchar("owner_type", 64)

    override val primaryKey = PrimaryKey(id)
}