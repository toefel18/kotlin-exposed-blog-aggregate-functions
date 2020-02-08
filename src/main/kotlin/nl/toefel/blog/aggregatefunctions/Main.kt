package nl.toefel.blog.aggregatefunctions

import com.fasterxml.jackson.databind.SerializationFeature
import io.javalin.Javalin
import io.javalin.core.JavalinConfig
import io.javalin.http.Context
import io.javalin.plugin.json.JavalinJackson
import nl.toefel.blog.aggregatefunctions.db.CommentTable
import nl.toefel.blog.aggregatefunctions.db.UserTable
import nl.toefel.blog.aggregatefunctions.dto.CommentDto
import nl.toefel.blog.aggregatefunctions.dto.UserPopularityScore
import nl.toefel.blog.aggregatefunctions.dto.UserStatistic
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.div
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.times
import org.jetbrains.exposed.sql.avg

/**
 * Starts an in-memory H2 database, creates the schema and loads some test data and exposes a HTTP API
 */
fun main(args: Array<String>) {
    Database.connect("jdbc:h2:mem:regular;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
    DatabaseInitializer.createSchemaAndTestData()
    Router(8080).start()
}

/**
 * Creates the webserver:
 * 1. configures a request logger
 * 2. configures available paths and their handlers
 * 3. transforms database results to DTOs
 */
class Router(val port: Int) {
    private val logger: Logger = LoggerFactory.getLogger(Router::class.java)

    private val app: Javalin = Javalin.create(this::configureJavalin)
        .get("/comments", ::listComments)
        .get("/user-statistics", ::userStatistics)
        .get("/user-popularity", ::userPopularity)

    fun start(): Router {
        app.start(port)
        println("started on port $port, visit:\n" +
            "http://localhost:8080/comments \n" +
            "http://localhost:8080/user-statistics " +
            "http://localhost:8080/user-popularity ")
            return this
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

val commentPopularityScore = CommentTable.likes.times(2).minus(CommentTable.dislikes).div(2)

CommentTable
    .innerJoin(UserTable)
    .slice(UserTable.name, commentPopularityScore.avg(2))
    .selectAll()
    .groupBy(UserTable.name)
    .map { row ->
        UserPopularityScore(
            fromUser = row[UserTable.name],
            popularityScore = row[commentPopularityScore.sum()] ?: 0L
        )
    }
        }

        ctx.json(userPopularityScores)
    }

    private fun configureJavalin(cfg: JavalinConfig) {
        cfg.requestLogger(::logRequest).enableCorsForAllOrigins();
        JavalinJackson.getObjectMapper().findAndRegisterModules()
        JavalinJackson.getObjectMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        JavalinJackson.getObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true)
    }

    private fun logRequest(ctx: Context, executionTimeMs: Float) =
        logger.info("${ctx.method()} ${ctx.fullUrl()} status=${ctx.status()} durationMs=$executionTimeMs")

}
