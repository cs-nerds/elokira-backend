package model

import org.jetbrains.exposed.sql.Table
import java.util.UUID

object Logins : Table() {
    val loginId = uuid("loginId")
    val userId = reference("userId", Users.userId)
    val loginCode = varchar("loginCode", 32)
    val activated = bool("activated")
    override val primaryKey = PrimaryKey(loginId)
}

data class Login(
    val loginId: UUID? = UUID.randomUUID(),
    val userId: UUID?,
    val loginCode: String,
    var activated: Boolean? = false
)



data class LoginRequest(
    val idNumber: String
)
