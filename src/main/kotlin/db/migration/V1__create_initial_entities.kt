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
            val secondUserId = UUID.randomUUID()
            val thirdUserId = UUID.randomUUID()
            val electionID = UUID.randomUUID()
            val positionId1 = UUID.randomUUID()
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
            Users.insert {
                it[userId] = secondUserId
                it[firstName] = System.getenv("USER1_FIRST_NAME")
                it[lastName] = System.getenv("USER1_LAST_NAME")
                it[phoneNumber] = System.getenv("USER1_PHONE_NUMBER")
                it[idNumber] = System.getenv("USER1_ID_NUMBER")
                it[admin] = false
                it[dateCreated] = System.currentTimeMillis()
                it[dateUpdated] = System.currentTimeMillis()
                it[lastUpdatedBy] = firstUserId
            }
            Users.insert {
                it[userId] = thirdUserId
                it[firstName] = System.getenv("USER2_FIRST_NAME")
                it[lastName] = System.getenv("USER2_LAST_NAME")
                it[phoneNumber] = System.getenv("USER2_PHONE_NUMBER")
                it[idNumber] = System.getenv("USER2_ID_NUMBER")
                it[admin] = false
                it[dateCreated] = System.currentTimeMillis()
                it[dateUpdated] = System.currentTimeMillis()
                it[lastUpdatedBy] = firstUserId
            }

            Elections.insert {
                it[electionId] = electionID
                it[electionName] = "Demo General Election"
                it[startDate] = System.currentTimeMillis()
                it[stopDate] = System.currentTimeMillis()
                it[createdBy] = firstUserId
                it[dateModified] = System.currentTimeMillis()
            }

            Positions.insert {
                it[positionId] = positionId1
                it[positionName] = "President"
                it[electionId] = electionID
                it[createdBy] = firstUserId
                it[dateModified] = System.currentTimeMillis()
            }

            Candidates.insert {
                it[candidateId] = UUID.randomUUID()
                it[userId] = firstUserId
                it[positionId] = positionId1
                it[verified] = true
                it[verifiedBy] = firstUserId
                it[registrationDate] = System.currentTimeMillis()
            }

            Candidates.insert {
                it[candidateId] = UUID.randomUUID()
                it[userId] = secondUserId
                it[positionId] = positionId1
                it[verified] = true
                it[verifiedBy] = firstUserId
                it[registrationDate] = System.currentTimeMillis()
            }

            Candidates.insert {
                it[candidateId] = UUID.randomUUID()
                it[userId] = thirdUserId
                it[positionId] = positionId1
                it[verified] = true
                it[verifiedBy] = firstUserId
                it[registrationDate] = System.currentTimeMillis()
            }
        }
    }
}