package model

import org.jetbrains.exposed.sql.Table

object Candidates : Table() {
    val candidateId = uuid("candidateId").uniqueIndex()
    val userId = reference("userId", Users.userId)
    val positionId = reference("positionId", Positions.positionId)
    val registrationDate = long("registrationDate")
    val verified = bool("verified")
    val verifiedBy = reference("verifiedBy", Users.userId).nullable()
    override val primaryKey = PrimaryKey(userId, positionId)
}