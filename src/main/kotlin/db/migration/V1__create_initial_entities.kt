package db.migration

import model.*
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class V1__create_initial_entities: BaseJavaMigration() {
    override fun migrate(context: Context?) {
        transaction {
            // Create Tables
            SchemaUtils.create(Users)
            SchemaUtils.create(Logins)
            SchemaUtils.create(Elections)
            SchemaUtils.create(Positions)
            SchemaUtils.create(Candidates)
            SchemaUtils.create(Voters)

            // Create an Admin User
            val firstUserId = UUID.randomUUID()
            Users.insert {
                it[userId] = firstUserId
                it[firstName] = System.getenv("ADMIN_FIRST_NAME")
                it[lastName] = System.getenv("ADMIN_LAST_NAME")
                it[phoneNumber] = System.getenv("ADMIN_PHONE_NUMBER")
                it[idNumber] = System.getenv("ADMIN_ID_NUMBER")
                it[admin] = true
                it[dateCreated] = System.currentTimeMillis()
                it[dateUpdated] = System.currentTimeMillis()
                it[lastUpdatedBy] = firstUserId
            }
        }
    }
}