package model

import org.jetbrains.exposed.sql.Table
import java.util.*

object Candidates : Table() {
    val candidateId = uuid("candidateId").uniqueIndex()
    val userId = reference("userId", Users.userId)
    val positionId = reference("positionId", Positions.positionId)
    val registrationDate = long("registrationDate")
    val verified = bool("verified")
    val verifiedBy = reference("verifiedBy", Users.userId).nullable()
    override val primaryKey = PrimaryKey(userId, positionId)
}

data class Candidate(
    val candidateId: UUID,
    val userId: UUID,
    val positionId: UUID,
    val registrationDate: Long,
    val verified: Boolean,
    val verifiedBy: UUID?
)

data class CandidateDetails(
    val candidateId: UUID,
    val positionId: UUID,
    val candidateFirstName: String,
    val candidateLastName: String,
    val positionName: String
)