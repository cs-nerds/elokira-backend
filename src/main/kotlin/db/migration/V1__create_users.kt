package db.migration

import model.Logins
import model.Users
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class V1__create_users: BaseJavaMigration() {
    override fun migrate(context: Context?) {
        transaction {
            SchemaUtils.create(Users)
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

            SchemaUtils.create(Logins)
        }
    }
}