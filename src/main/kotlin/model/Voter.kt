package model

import org.jetbrains.exposed.sql.Table
import java.util.*

object Voters : Table() {
    val voterId = uuid("voterId").uniqueIndex()
    val userId = reference("userId", Users.userId)
    val electionId = reference("electionId", Elections.electionId)
    val registrationDate = long("registrationDate")
    val voted = bool("voted")
    override val primaryKey = PrimaryKey(userId, electionId)
}

data class Voter (
    val voterId: UUID,
    val userId: UUID,
    val electionId: UUID,
    val registrationDate: Long,
    val voted: Boolean
)

data class NewVoter(
    val userId: UUID,
    val electionId: UUID,
)