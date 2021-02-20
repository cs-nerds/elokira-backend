package model

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val userId = integer("userId").autoIncrement()
    val firstName = varchar("firstName", 128)
    val lastName = varchar("lastName", 128)
    val phoneNumber = varchar("phoneNumber", 30).uniqueIndex()
    val idNumber = varchar("idNumber", 30).uniqueIndex()
    val dateUpdated = long("dateUpdated")
    override val primaryKey = PrimaryKey(userId)
}

data class User(
    val userId: Int,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val idNumber: String,
    val dateUpdated: Long
)

data class NewUser(
    val userId: Int?,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val idNumber: String
)

data class UnverifiedUser(
    val firstName: String,
    val idNumber: String,
    val verified: Boolean? = false
)

data class VerifiedUser(
    val firstName: String,
    val lastName: String,
    val idNumber: String,
    val verified: Boolean = true
)