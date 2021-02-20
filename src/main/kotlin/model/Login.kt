package model

import org.jetbrains.exposed.sql.Table

object Logins : Table() {
    val loginId = integer("loginId").autoIncrement()
    val userId = reference("userId", Users.userId)
    val loginCode = integer("loginCode")
    val activated = bool("activated")
    override val primaryKey = PrimaryKey(loginId)
}

data class Login(
    val loginId: Int?,
    val userId: Int?,
    val loginCode: Int,
    var activated: Boolean? = false
)

data class LoginRequest(
    val idNumber: String
)
