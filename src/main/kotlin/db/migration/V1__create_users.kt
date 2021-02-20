package db.migration

import model.Logins
import model.Users
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class V1__create_users: BaseJavaMigration() {
    override fun migrate(context: Context?) {
        transaction {
            SchemaUtils.create(Users)

            Users.insert {
                it[firstName] = System.getenv("ADMIN_FIRST_NAME")
                it[lastName] = System.getenv("ADMIN_LAST_NAME")
                it[phoneNumber] = System.getenv("ADMIN_PHONE_NUMBER")
                it[idNumber] = System.getenv("ADMIN_ID_NUMBER")
                it[dateUpdated] = System.currentTimeMillis()
            }

            SchemaUtils.create(Logins)
        }
    }
}