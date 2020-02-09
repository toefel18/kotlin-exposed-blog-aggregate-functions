package nl.toefel.blog.aggregatefunctions

import nl.toefel.blog.aggregatefunctions.db.CommentTable
import nl.toefel.blog.aggregatefunctions.db.CommentTable.content
import nl.toefel.blog.aggregatefunctions.db.CommentTable.dislikes
import nl.toefel.blog.aggregatefunctions.db.CommentTable.fromUser
import nl.toefel.blog.aggregatefunctions.db.CommentTable.likes
import nl.toefel.blog.aggregatefunctions.db.UserTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Creates the schema and loads some test data
 */
object DatabaseInitializer {
    val logger: Logger = LoggerFactory.getLogger(DatabaseInitializer::class.java)

    fun createSchemaAndTestData() {
        logger.info("Creating/Updating schema")

        transaction {
            SchemaUtils.createMissingTablesAndColumns(UserTable, CommentTable)
        }

        val users = transaction {
            UserTable.selectAll().count()
        }

        if (users > 0) {
            logger.info("There appears to be data already present, not inserting test data!")
            return
        }

        logger.info("Inserting test transaction")

        transaction {
            val hector = UserTable.insert {
                it[name] = "Hector"
            } get UserTable.id

            val charlotte = UserTable.insert {
                it[name] = "Charlotte"
            } get UserTable.id

            val john = UserTable.insert {
                it[name] = "John"
            } get UserTable.id

            val userIds = listOf(hector, charlotte, john)
            // generate an infinite sequence by using userIds as the seed and next function
            // by flattening it we will receive one item per time, then an iterator because
            // it's easy to use in the code below
            val userIdSequence = generateSequence(userIds) { userIds }.flatten().iterator()

            CommentTable.batchInsert(fortuneCookieMessages) { message ->
                this[fromUser] = userIdSequence.next()
                this[content] = message
                this[likes] = message.count { "aeo".contains(it) }.toLong()
                this[dislikes] = message.count { "iuy".contains(it) }.toLong()
            }
        }
    }

    // taken from http://www.fortunecookiemessage.com/archive.php
    val fortuneCookieMessages = listOf(
        "Today it's up to you to create the peacefulness you long for.",
        "A friend asks only for your time not your money.",
        "If you refuse to accept anything but the best, you very often get it.",
//        "A smile is your passport into the hearts of others.",
//        "A good way to keep healthy is to eat more Chinese food.",
//        "Your high-minded principles spell success.",
//        "Hard work pays off in the future, laziness pays off now.",
//        "Change can hurt, but it leads a path to something better.",
//        "Enjoy the good luck a companion brings you.",
//        "People are naturally attracted to you.",
//        "Hidden in a valley beside an open stream- This will be the type of place where you will find your dream.",
//        "A chance meeting opens new doors to success and friendship.",
//        "You learn from your mistakes... You will learn a lot today.",
//        "If you have something good in your life, don't let it go!",
//        "What ever you're goal is in life, embrace it visualize it, and for it will be yours.",
//        "Your shoes will make you happy today.",
//        "You cannot love life until you live the life you love.",
//        "Be on the lookout for coming events; They cast their shadows beforehand.",
//        "Land is always on the mind of a flying bird.",
//        "The man or woman you desire feels the same about you.",
//        "Meeting adversity well is the source of your strength.",
//        "A dream you have will come true.",
//        "Our deeds determine us, as much as we determine our deeds.",
//        "Never give up. You're not a failure if you don't give up.",
//        "You will become great if you believe in yourself.",
//        "There is no greater pleasure than seeing your loved ones prosper.",
//        "You will marry your lover.",
//        "A very attractive person has a message for you.",
//        "You already know the answer to the questions lingering inside your head.",
        "It is now, and in this world, that we must live.")
}