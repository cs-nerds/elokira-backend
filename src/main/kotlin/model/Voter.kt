package model

import org.jetbrains.exposed.sql.Table

object Voters : Table() {
    val voterId = uuid("voterId").uniqueIndex()
    val userId = reference("userId", Users.userId)
    val electionId = reference("electionId", Elections.electionId)
    val registrationDate = long("registrationDate")
    val voted = bool("voted")
    override val primaryKey = PrimaryKey(userId, electionId)
}